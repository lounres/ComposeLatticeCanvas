package dev.lounres.composeLatticeCanvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.lounres.kone.misc.lattices.Position


public abstract class LatticeCanvas<C, K> {
    public abstract val kinds: Set<K>
    public abstract fun fieldCoordinatesToLatticeCoordinates(fieldOffset: Offset): Offset
    public abstract fun latticeCoordinatesToFieldCoordinates(latticeOffset: Offset): Offset
    public abstract fun latticeCoordinatesToPosition(latticeOffset: Offset): Position<C, K>
    public abstract fun discreteLatticeCoordinatesToPositionCoordinates(latticeOffset: IntOffset): C
    public abstract fun screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
        latticeOffset00: Offset,
        latticeOffset10: Offset,
        latticeOffset01: Offset,
        latticeOffset11: Offset
    ): Sequence<IntOffset>
    public abstract fun contourOf(kind: K, tileActualSize: Float): Path
    public abstract fun DrawScope.cellContours(latticeOffset: IntOffset, tileActualSize: Float)


    @Composable
    public fun Content(
        modifier: Modifier = Modifier,
        tileSize: Dp = 64.dp,
        zoomMin: Float = 0.1f,
        zoomMax: Float = 10f,
        latticeCanvasState: LatticeCanvasState = rememberLatticeCanvasState(),
        onCellClick: (position: Position<C, K>) -> Unit = { _ -> },
        onCellDraw: DrawScope.(position: Position<C, K>, tileContour: Path, tileSize: Float) -> Unit = { _, _, _ -> },
        onDrawAbove: DrawScope.(fieldOffset: Offset, tileSize: Float) -> Unit = { _, _ -> }
    ) {
        val tileSizePx = LocalDensity.current.run { tileSize.toPx() }

        var fieldOffset by latticeCanvasState::fieldOffset
        var fieldZoom by latticeCanvasState::fieldZoom
        val tileActualSize by remember { derivedStateOf { tileSizePx * fieldZoom } }

        Canvas(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val zoomDelta = -event.changes.last().scrollDelta.y / 10
                                val pointer = event.changes.last().position.let { Offset(-size.width/2 + it.x, size.height/2 - it.y) }
                                if (fieldZoom + zoomDelta in zoomMin..zoomMax) {
                                    // zoom
                                    val oldZoom = fieldZoom
                                    fieldZoom += zoomDelta

                                    //mouse centered resize
                                    val newLeftTop = fieldOffset * tileActualSize / oldZoom * fieldZoom
                                    val oldPointer = (pointer + fieldOffset * tileActualSize) / oldZoom
                                    val newPointer = (pointer + newLeftTop) / fieldZoom
                                    val offDelta = newPointer - oldPointer
                                    fieldOffset = (newLeftTop - offDelta * fieldZoom) / tileActualSize
                                }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ -> fieldOffset -= Offset(pan.x, -pan.y) / tileActualSize }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val actualOffset = Offset((-size.width/2 + it.x) / tileActualSize + fieldOffset.x, (size.height/2 - it.y) / tileActualSize + fieldOffset.y)
                            onCellClick(latticeCoordinatesToPosition(fieldCoordinatesToLatticeCoordinates(actualOffset)))
                        }
                    )
                }
        ) {
            clipRect {
                val cellsToDraw = screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
                    latticeOffset00 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(-size.width/2, -size.height/2) / tileActualSize),
                    latticeOffset10 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(size.width/2, -size.height/2) / tileActualSize),
                    latticeOffset01 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(-size.width/2, size.height/2) / tileActualSize),
                    latticeOffset11 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(size.width/2, size.height/2) / tileActualSize),
                )

                translate(size.width/2 - fieldOffset.x * tileActualSize, -size.height/2 + fieldOffset.y * tileActualSize) {
                    for (cell in cellsToDraw) {
                        val (startX, startY) = latticeCoordinatesToFieldCoordinates(Offset(cell.x.toFloat(), cell.y.toFloat())) * tileActualSize
                        translate(startX, size.height - startY) { cellContours(cell, tileActualSize) }
                    }

                    for (cell in cellsToDraw) {
                        val (fieldX, fieldY) = latticeCoordinatesToFieldCoordinates(Offset(cell.x.toFloat(), cell.y.toFloat())) * tileActualSize
                        translate(fieldX, size.height - fieldY) {
                            for (kind in kinds) {
                                val contour = contourOf(kind, tileActualSize)
                                clipPath(path = contour) {
                                    onCellDraw(
                                        Position(discreteLatticeCoordinatesToPositionCoordinates(cell), kind),
                                        contour,
                                        tileActualSize
                                    )
                                }
                            }
                        }
                    }
                    onDrawAbove(fieldOffset, tileActualSize)
                }
            }
        }
    }
}