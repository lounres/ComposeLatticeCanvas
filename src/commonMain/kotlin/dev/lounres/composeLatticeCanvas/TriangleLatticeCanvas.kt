package dev.lounres.composeLatticeCanvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import dev.lounres.kone.misc.lattices.Position
import dev.lounres.kone.misc.lattices.TriangleKind
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


public object TriangleLatticeCanvas: LatticeCanvas<Pair<Int, Int>, TriangleKind>() {
    private const val oneOverSqrtThree: Float = 0.57735026f
    private const val sqrtThree: Float = 1.7320508f

    override val kinds: Set<TriangleKind> = TriangleKind.entries.toSet()
    override fun fieldCoordinatesToLatticeCoordinates(fieldOffset: Offset): Offset =
        Offset(
            (fieldOffset.x - oneOverSqrtThree * fieldOffset.y),
            (2 * oneOverSqrtThree * fieldOffset.y),
        )
    override fun latticeCoordinatesToFieldCoordinates(latticeOffset: Offset): Offset =
        Offset(
            (latticeOffset.x + latticeOffset.y / 2),
            (sqrtThree / 2 * latticeOffset.y),
        )
    override fun latticeCoordinatesToPosition(latticeOffset: Offset): Position<Pair<Int, Int>, TriangleKind> {
        val latticeX = floor(latticeOffset.x)
        val latticeY = floor(latticeOffset.y)
        val kind = if ((latticeOffset.x - latticeX) + (latticeOffset.y - latticeY) > 1) TriangleKind.Down else TriangleKind.Up
        return Position(Pair(latticeX.toInt(), latticeY.toInt()), kind)
    }
    override fun discreteLatticeCoordinatesToPositionCoordinates(latticeOffset: IntOffset): Pair<Int, Int> = Pair(latticeOffset.x, latticeOffset.y)
    override fun screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
        latticeOffset00: Offset,
        latticeOffset10: Offset,
        latticeOffset01: Offset,
        latticeOffset11: Offset
    ): Sequence<IntOffset>  = sequence {
        // TODO: Optimise the turning over cells.
        val startCell = IntOffset(
            floor(min(min(latticeOffset00.x, latticeOffset10.x), min(latticeOffset01.x, latticeOffset11.x))).toInt(),
            floor(min(min(latticeOffset00.y, latticeOffset10.y), min(latticeOffset01.y, latticeOffset11.y))).toInt(),
        )

        val endCell = IntOffset(
            ceil(max(max(latticeOffset00.x, latticeOffset10.x), max(latticeOffset01.x, latticeOffset11.x))).toInt(),
            ceil(max(max(latticeOffset00.y, latticeOffset10.y), max(latticeOffset01.y, latticeOffset11.y))).toInt(),
        )

        for (xi in startCell.x .. endCell.x) for (yi in startCell.y .. endCell.y) yield(IntOffset(xi, yi))
    }

    override fun contourOf(kind: TriangleKind, tileActualSize: Float): Path =
        when(kind) {
            TriangleKind.Up -> Path().apply {
                moveTo(0f, 0f)
                lineTo(tileActualSize, 0f)
                lineTo(tileActualSize / 2,-tileActualSize * sqrtThree / 2)
                close()
            }
            TriangleKind.Down -> Path().apply {
                moveTo(tileActualSize * 3 / 2, -tileActualSize * sqrtThree / 2)
                lineTo(tileActualSize, 0f)
                lineTo(tileActualSize / 2,-tileActualSize * sqrtThree / 2)
                close()
            }
        }

    override fun DrawScope.cellContours(latticeOffset: IntOffset, tileActualSize: Float) {
        drawPath(
            path = Path().apply {
                moveTo(0f, 0f)
                lineTo(tileActualSize, 0f)
                lineTo(tileActualSize / 2,-tileActualSize * sqrtThree / 2)
                close()
            },
            color = Color.Black,
            style = Stroke(),
        )
    }
}