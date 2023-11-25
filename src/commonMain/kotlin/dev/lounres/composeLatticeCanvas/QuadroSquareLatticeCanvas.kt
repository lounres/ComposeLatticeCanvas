package dev.lounres.composeLatticeCanvas

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.IntOffset
import dev.lounres.kone.misc.lattices.Position
import dev.lounres.kone.misc.lattices.QuadroSquareKind
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min


public object QuadroSquareLatticeCanvas: LatticeCanvas<Pair<Int, Int>, QuadroSquareKind>() {
    override val kinds: Set<QuadroSquareKind> = QuadroSquareKind.entries.toSet()
    override fun fieldCoordinatesToLatticeCoordinates(fieldOffset: Offset): Offset = fieldOffset
    override fun latticeCoordinatesToFieldCoordinates(latticeOffset: Offset): Offset = latticeOffset
    override fun latticeCoordinatesToPosition(latticeOffset: Offset): Position<Pair<Int, Int>, QuadroSquareKind> {
        val latticeX = floor(latticeOffset.x)
        val latticeY = floor(latticeOffset.y)
        val restX = latticeOffset.x - latticeX
        val restY = latticeOffset.y - latticeY
        val kind = when {
            restX + restY > 1 && restX > restY -> QuadroSquareKind.Right
            restX + restY > 1 && restX <= restY -> QuadroSquareKind.Up
            restX + restY < 1 && restX > restY -> QuadroSquareKind.Down
            else -> QuadroSquareKind.Left
        }
        return Position(Pair(latticeX.toInt(), latticeY.toInt()), kind)
    }
    override fun discreteLatticeCoordinatesToPositionCoordinates(latticeOffset: IntOffset): Pair<Int, Int> = Pair(latticeOffset.x, latticeOffset.y)
    override fun screenLatticeCoordinatesToDiscreteLatticeCoordinatesSequence(
        latticeOffset00: Offset,
        latticeOffset10: Offset,
        latticeOffset01: Offset,
        latticeOffset11: Offset
    ): Sequence<IntOffset>  = sequence {
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

    override fun contourOf(kind: QuadroSquareKind, tileActualSize: Float): Path =
        when(kind) {
            QuadroSquareKind.Up -> Path().apply {
                moveTo(0f, -tileActualSize)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(tileActualSize, -tileActualSize)
                close()
            }
            QuadroSquareKind.Down -> Path().apply {
                moveTo(0f, 0f)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(tileActualSize, 0f)
                close()
            }
            QuadroSquareKind.Left -> Path().apply {
                moveTo(0f, -tileActualSize)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(0f, 0f)
                close()
            }
            QuadroSquareKind.Right -> Path().apply {
                moveTo(tileActualSize, -tileActualSize)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(tileActualSize, 0f)
                close()
            }
        }

    override fun DrawScope.cellContours(latticeOffset: IntOffset, tileActualSize: Float) {
        drawPath(
            path = Path().apply {
                moveTo(0f, 0f)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(tileActualSize,0f)
                lineTo(0f, 0f)
                lineTo(0f, -tileActualSize)
                lineTo(tileActualSize/2, -tileActualSize/2)
                lineTo(tileActualSize, -tileActualSize)
            },
            color = Color.Black,
            style = Stroke(),
        )
    }
}