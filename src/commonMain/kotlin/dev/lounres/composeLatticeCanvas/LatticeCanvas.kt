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
    protected abstract val kinds: Set<K>
    protected abstract fun fieldCoordinatesToLatticeCoordinates(fieldOffset: Offset, tileActualSize: Float): Offset
    protected abstract fun latticeCoordinatesToFieldCoordinates(latticeOffset: Offset, tileActualSize: Float): Offset
    protected abstract fun latticeCoordinatesToPosition(latticeOffset: Offset): Position<C, K>
    protected abstract fun discreteLatticeCoordinatesToPositionCoordinates(latticeOffset: IntOffset): C
    protected abstract fun screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
        latticeOffset00: Offset,
        latticeOffset10: Offset,
        latticeOffset01: Offset,
        latticeOffset11: Offset
    ): Sequence<IntOffset>
    protected abstract fun contourOf(kind: K, tileActualSize: Float): Path
    protected abstract fun DrawScope.cellContours(latticeOffset: IntOffset, tileActualSize: Float)


    @Composable
    public fun Content(
        modifier: Modifier = Modifier,
        tileSize: Dp = 64.dp,
        zoomMin: Float = 0.1f,
        zoomMax: Float = 10f,
        onCellClick: (position: Position<C, K>) -> Unit = { _ -> },
        onCellDraw: DrawScope.(position: Position<C, K>, tileSize: Float) -> Unit = { _, _ -> },
        onDrawAbove: DrawScope.(fieldOffset: Offset, tileSize: Float) -> Unit = { _, _ -> }
    ) {
        val tileSizePx = LocalDensity.current.run { tileSize.toPx() }

        var fieldOffset by remember { mutableStateOf(Offset(0f, 0f)) }
        var fieldZoom by remember { mutableStateOf(1f) }
        val tileActualSize by remember { derivedStateOf { tileSizePx * fieldZoom } }

        Canvas(
            modifier = modifier
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            if (event.type == PointerEventType.Scroll) {
                                val zoomDelta = -event.changes.last().scrollDelta.y / 10
                                val pointer = event.changes.last().position.let { Offset(it.x, size.height - it.y) }
                                if (fieldZoom + zoomDelta in zoomMin..zoomMax) {
                                    // zoom
                                    val oldZoom = fieldZoom
                                    fieldZoom += zoomDelta

                                    //mouse centered resize
                                    val newLeftTop = (fieldOffset / oldZoom) * fieldZoom
                                    val oldPointer = (pointer + fieldOffset) / oldZoom
                                    val newPointer = (pointer + newLeftTop) / fieldZoom
                                    val offDelta = newPointer - oldPointer
                                    fieldOffset = newLeftTop - offDelta * fieldZoom
                                }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, _, _ -> fieldOffset -= Offset(pan.x, -pan.y) }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            val actualOffset = Offset(it.x + fieldOffset.x, size.height - it.y + fieldOffset.y)
                            onCellClick(latticeCoordinatesToPosition(fieldCoordinatesToLatticeCoordinates(actualOffset, tileActualSize)))
                        }
                    )
                }
        ) {
            clipRect {
                val cellsToDraw = screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
                    latticeOffset00 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(-size.width/2, -size.height/2), tileActualSize),
                    latticeOffset10 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(size.width/2, -size.height/2), tileActualSize),
                    latticeOffset01 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(-size.width/2, size.height/2), tileActualSize),
                    latticeOffset11 = fieldCoordinatesToLatticeCoordinates(fieldOffset + Offset(size.width/2, size.height/2), tileActualSize),
                )

                translate(size.width/2-fieldOffset.x, -size.height/2+fieldOffset.y) {
                    for (cell in cellsToDraw) {
                        val (startX, startY) = latticeCoordinatesToFieldCoordinates(Offset(cell.x.toFloat(), cell.y.toFloat()), tileActualSize)
                        translate(startX, size.height - startY) { cellContours(cell, tileActualSize) }
                    }

                    for (cell in cellsToDraw) {
                        val (fieldX, fieldY) = latticeCoordinatesToFieldCoordinates(Offset(cell.x.toFloat(), cell.y.toFloat()), tileActualSize)
                        translate(fieldX, size.height - fieldY) {
                            for (kind in kinds)
                                clipPath(path = contourOf(kind, tileActualSize)) {
                                    onCellDraw(Position(discreteLatticeCoordinatesToPositionCoordinates(cell), kind), tileActualSize)
                                }
                        }
                    }
                    onDrawAbove(fieldOffset, tileActualSize)
                }
            }
        }
    }
}