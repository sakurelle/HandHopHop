package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.design.R as DesignR
import androidx.compose.foundation.lazy.items
@Composable
fun FilterSection(
    filterSectionState: FilterSectionState,
    filterCondition: (id: Int) -> Unit
) {
    val colors = HandHopHopDesignSystem.colors
    val padding = dimensionResource(DesignR.dimen.filter_section_padding)
    val fontsize = dimensionResource(DesignR.dimen.filter_option_text_size).value.sp
    val spase = dimensionResource(DesignR.dimen.filter_section_padding)


    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            modifier = Modifier.padding(start = padding, bottom = padding),
            text = stringResource(filterSectionState.titleRes),
            color = colors.textPrimary,
            fontSize = fontsize
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = padding),
            horizontalArrangement = Arrangement.spacedBy(space = spase)
        ) {
            items(filterSectionState.options) { option ->
                FilterOption(
                    filterOptionState = option,
                    filterCondition = filterCondition
                )
            }
        }
    }
}
