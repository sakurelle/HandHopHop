package ru.handhophop.feature.mash

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.handhophop.feature.mash.MashCreate.MashThread

@Immutable
internal data class MashUiState(
    val isLoading: Boolean = false,
    val scheme: SchemeData? = null,
    val visiblePalette: List<MashThread> = emptyList(),
    @StringRes val errorTextRes: Int? = null,
    val isDownloadButtonEnabled: Boolean = false,
    val isPaletteVisible: Boolean = false,
    val selectedPaletteIndex: Int? = null,
    val completedPaletteIndices: Set<Int> = emptySet(),
)

@Immutable
internal data class SchemeData(
    val gridW: Int,
    val gridH: Int,
    val palette: List<MashThread>,
    val indices: IntArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SchemeData) return false

        return gridW == other.gridW &&
                gridH == other.gridH &&
                palette == other.palette &&
                indices.contentEquals(other.indices)
    }

    override fun hashCode(): Int {
        var result = gridW
        result = 31 * result + gridH
        result = 31 * result + palette.hashCode()
        result = 31 * result + indices.contentHashCode()
        return result
    }
}
