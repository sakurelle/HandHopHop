package ru.handhophop.feature.feed.presentation

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import ru.handhophop.feature.feed.R

@Immutable
data class FeedFilter(
    val color: ColorFilter = ColorFilter.ANY,
    val orientation: OrientationFilter = OrientationFilter.ANY,
    val category: CategoryFilter = CategoryFilter.GENERAL,
    val sorting: SortingFilter = SortingFilter.RANDOM,
)

enum class CategoryFilter(val id: Int, val code: String, @StringRes val labelRes: Int) {
    GENERAL(0, "100", R.string.category_general),
    ANIME(1, "010", R.string.category_anime),
    PEOPLE(2, "001", R.string.category_people),
    ALL(3, "111", R.string.category_all),
}

enum class SortingFilter(val id: Int, val value: String, @StringRes val labelRes: Int) {
    RANDOM(0, "random", R.string.sorting_random),
    RELEVANCE(1, "relevance", R.string.sorting_relevance),
    DATE_ADDED(2, "date_added", R.string.sorting_date_added),
    VIEWS(3, "views", R.string.sorting_views),
    FAVORITES(4, "favorites", R.string.sorting_favorites),
    TOPLIST(5, "toplist", R.string.sorting_toplist),
}

enum class ColorFilter(val id: Int, @StringRes val labelRes: Int) {
    ANY(0, R.string.all_option),
    BLACK(1, R.string.black_option),
    BLUE(2, R.string.blue_option),
    GRAY(3, R.string.gray_option),
    GREEN(4, R.string.green_option),
    ORANGE(5, R.string.orange_option),
    RED(6, R.string.red_option),
    WHITE(7, R.string.white_option),
    YELLOW(8, R.string.yellow_option),
    PURPLE(9, R.string.purple_option),
    CYAN(10, R.string.cyan_option),
    PINK(11, R.string.pink_option),
}

enum class OrientationFilter(val id: Int, @StringRes val labelRes: Int) {
    ANY(0, R.string.all_option),
    LANDSCAPE(1, R.string.landscape_option),
    PORTRAIT(2, R.string.portrait_option),
    SQUARE(3, R.string.square_option),
    PANORAMIC(4, R.string.panoramic_option),
}
