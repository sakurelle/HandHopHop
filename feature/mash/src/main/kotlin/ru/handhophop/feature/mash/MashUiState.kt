package ru.handhophop.feature.mash

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import ru.handhophop.feature.mash.complexity.ComplexityType

@Immutable
internal data class MashUiState(
    val isLoading: Boolean = false,
    val scheme: SchemeData? = null,
    val visiblePalette: List<Color> = emptyList(),
    @StringRes val errorTextRes: Int? = null,
    val isDownloadButtonEnabled: Boolean = false,
    val isPaletteVisible: Boolean = false,
    val reachedEnd: Boolean = false,
    val isSquareFilterEnabled: Boolean = false,
    val items: ImageItem? = null,
    val selectedComplexity: ComplexityType = ComplexityType.EASY,
    val sourceImageUrl: String? = null,
)

@Immutable
internal data class ImageItem(
    val id: String,
    val imageUrl: String,
    val aspectRatio: Float,
    val author: String = ""
)

@Immutable
internal data class SchemeData(
    val gridW: Int,
    val gridH: Int,
    val palette: List<Color>,
    val indices: IntArray
    //Мне так посоветовал IDE, у нас же IntArray и Immutable
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SchemeData

        if (gridW != other.gridW) return false
        if (gridH != other.gridH) return false
        if (palette != other.palette) return false
        if (!indices.contentEquals(other.indices)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = gridW
        result = 31 * result + gridH
        result = 31 * result + palette.hashCode()
        result = 31 * result + indices.contentHashCode()
        return result
    }
}