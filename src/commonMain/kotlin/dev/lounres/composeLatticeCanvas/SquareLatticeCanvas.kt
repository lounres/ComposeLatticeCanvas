package dev.lounres.composeLatticeCanvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import dev.lounres.kone.misc.lattices.Position
import dev.lounres.kone.misc.lattices.SquareKind
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


public object SquareLatticeCanvas: LatticeCanvas<Pair<Int, Int>, SquareKind>() {
    override val kinds: Set<SquareKind> = setOf(SquareKind)
    override fun fieldCoordinatesToLatticeCoordinates(fieldOffset: Offset, tileActualSize: Float): Offset = fieldOffset / tileActualSize
    override fun latticeCoordinatesToFieldCoordinates(latticeOffset: Offset, tileActualSize: Float): Offset = latticeOffset * tileActualSize
    override fun latticeCoordinatesToPosition(latticeOffset: Offset): Position<Pair<Int, Int>, SquareKind> = Position(Pair(floor(latticeOffset.x).toInt(), floor(latticeOffset.y).toInt()), SquareKind)
    override fun discreteLatticeCoordinatesToPositionCoordinates(latticeOffset: IntOffset): Pair<Int, Int> = Pair(latticeOffset.x, latticeOffset.y)
    override fun screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
        latticeOffset00: Offset,
        latticeOffset10: Offset,
        latticeOffset01: Offset,
        latticeOffset11: Offset
    ): Sequence<IntOffset> = sequence {
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
    override fun contourOf(kind: SquareKind, tileActualSize: Float): Path = Path().apply {
        moveTo(0f, 0f)
        lineTo(tileActualSize, 0f)
        lineTo(tileActualSize, -tileActualSize)
        lineTo(0f, -tileActualSize)
        close()
    }
    override fun DrawScope.cellContours(latticeOffset: IntOffset, tileActualSize: Float) {
        drawPath(
            path = Path().apply {
                moveTo(tileActualSize, 0f)
                lineTo(0f, 0f)
                lineTo(0f, -tileActualSize)
            },
            color = Color.Black,
            style = Stroke(),
        )
    }

}