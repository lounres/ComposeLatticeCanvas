package dev.lounres.composeLatticeCanvas

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@Composable
public fun rememberLatticeCanvasState(
    fieldOffset: Offset = Offset(0f, 0f),
    fieldZoom: Float = 1f,
    tileUnit: Dp = 64.dp,
): LatticeCanvasState = rememberSaveable(saver = LatticeCanvasStateImpl.Saver()) {
    LatticeCanvasStateImpl(
        fieldOffset = fieldOffset,
        fieldZoom = fieldZoom,
        tileUnit = tileUnit,
    )
}

public fun LatticeCanvasState(
    fieldOffset: Offset = Offset(0f, 0f),
    fieldZoom: Float = 1f,
    tileUnit: Dp = 64.dp,
): LatticeCanvasState = LatticeCanvasStateImpl(
    fieldOffset = fieldOffset,
    fieldZoom = fieldZoom,
    tileUnit = tileUnit,
)

public interface LatticeCanvasState {
    public var fieldOffset: Offset
    public var fieldZoom: Float
    public var tileUnit: Dp
}

internal class LatticeCanvasStateImpl(
    fieldOffset: Offset,
    fieldZoom: Float,
    tileUnit: Dp,
): LatticeCanvasState {
    override var fieldOffset: Offset by mutableStateOf(fieldOffset)
    override var fieldZoom: Float by mutableStateOf(fieldZoom)
    override var tileUnit: Dp by mutableStateOf(tileUnit)

    companion object {
        fun Saver() = listSaver<LatticeCanvasState, Any>(
            save = {
                listOf(
                    it.fieldOffset,
                    it.fieldZoom,
                    it.tileUnit,
                )
            },
            restore = { state ->
                LatticeCanvasStateImpl(
                    fieldOffset = state[0] as Offset,
                    fieldZoom = state[1] as Float,
                    tileUnit = state[2] as Dp,
                )
            }
        )
    }
}