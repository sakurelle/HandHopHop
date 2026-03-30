package ru.handhophop.feature.mash.complexity

import androidx.compose.ui.graphics.Color

enum class ComplexityType(
    val title: String,
    val colorsCount: Int,
) {
    EASY(
        title = "Легкая",
        colorsCount = 4,
    ),
    MEDIUM(
        title = "Средняя",
        colorsCount = 12,
    ),
    HARD(
        title = "Сложная",
        colorsCount = 30,
    ),
}

object ComplexityData {

    val allColors = listOf(
        Color(0xFF6D4C41),
        Color(0xFF8D6E63),
        Color(0xFFA1887F),
        Color(0xFFBCAAA4),
        Color(0xFFD7CCC8),
        Color(0xFFEFEBE9),
        Color(0xFF5D4037),
        Color(0xFF4E342E),
        Color(0xFF3E2723),
        Color(0xFF8E24AA),

        Color(0xFFAB47BC),
        Color(0xFFBA68C8),
        Color(0xFFCE93D8),
        Color(0xFFE1BEE7),
        Color(0xFF7E57C2),
        Color(0xFF9575CD),
        Color(0xFFB39DDB),
        Color(0xFFD1C4E9),
        Color(0xFF5C6BC0),
        Color(0xFF7986CB),

        Color(0xFF9FA8DA),
        Color(0xFFC5CAE9),
        Color(0xFF42A5F5),
        Color(0xFF64B5F6),
        Color(0xFF90CAF9),
        Color(0xFFBBDEFB),
        Color(0xFF26A69A),
        Color(0xFF4DB6AC),
        Color(0xFF80CBC4),
        Color(0xFFB2DFDB),
    )

    fun getColorsByComplexity(complexity: ComplexityType): List<Color> {
        return allColors.take(complexity.colorsCount.coerceAtMost(allColors.size))
    }
}