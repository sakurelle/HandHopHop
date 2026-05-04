package ru.handhophop.feature.feed.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.handhophop.feature.feed.R

@Immutable
data class FeedFilter(
    val color: ColorFilter = ColorFilter.ANY,
    val orientation: OrientationFilter = OrientationFilter.ANY,
    val aiGenerated: AiGeneratedFilter = AiGeneratedFilter.ANY,
)

enum class AiGeneratedFilter(val id: Int, @StringRes val labelRes: Int) {
    ANY(0,      R.string.any_option),
    ONLY(1,     R.string.ai_only_option),
    EXCLUDED(2, R.string.ai_excluded_option)
}
enum class ColorFilter(val id: Int, @StringRes val labelRes: Int) {
    ANY(0,     R.string.all_option),
    BLACK(1,   R.string.black_option),
    BLUE(2,    R.string.blue_option),
    GRAY(3,    R.string.gray_option),
    GREEN(4,   R.string.green_option),
    ORANGE(5,  R.string.orange_option),
    RED(6,     R.string.red_option),
    WHITE(7,   R.string.white_option),
    YELLOW(8,  R.string.yellow_option),
    PURPLE(9,  R.string.purple_option),
    CYAN(10,   R.string.cyan_option),
    PINK(11,   R.string.pink_option)
}

enum class OrientationFilter(val id: Int, @StringRes val labelRes: Int) {
    ANY(0,       R.string.all_option),
    LANDSCAPE(1, R.string.landscape_option),
    PORTRAIT(2,  R.string.portrait_option),
    SQUARE(3,    R.string.square_option),
    PANORAMIC(4, R.string.panoramic_option)
}

