package ru.handhophop.feature.bookmark.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import ru.handhophop.feature.bookmark.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun BookmarkFilterRow (
    selectedFilter: BookmarkFilter,
    onFilterSelected: (BookmarkFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            horizontal = dimensionResource(DesignR.dimen.bookmark_filter_row_horizontal_padding),
            vertical = dimensionResource(DesignR.dimen.bookmark_filter_row_vertical_padding)
        ),
        horizontalArrangement = Arrangement.spacedBy(
            dimensionResource(DesignR.dimen.bookmark_filter_option_spacing)),
    ) {
        item {
            BookmarkFilterOption (
                text = stringResource(R.string.bookmark_filter_all),
                selected = selectedFilter == BookmarkFilter.ALL,
                onClick = { onFilterSelected(BookmarkFilter.ALL) },
            )
        }

        item {
            BookmarkFilterOption(
                text = stringResource(R.string.bookmark_filter_works),
                selected = selectedFilter == BookmarkFilter.WORKS,
                onClick = { onFilterSelected(BookmarkFilter.WORKS) },
            )
        }

        item {
            BookmarkFilterOption(
                text = stringResource(R.string.bookmark_filter_likes),
                selected = selectedFilter == BookmarkFilter.LIKES,
                onClick = { onFilterSelected(BookmarkFilter.LIKES) },
            )
        }
    }

}
