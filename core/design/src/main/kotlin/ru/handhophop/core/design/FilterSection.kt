package ru.handhophop.core.design

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.design.R

@Composable
fun FilterSection(
    filterSectionState: FilterSectionState,
    filterCondition: (name: String) -> Unit
) {
    val padding = dimensionResource(R.dimen.filter_section_padding)
    val fontsize = dimensionResource(R.dimen.filter_option_text_size).value.sp
    val spase = dimensionResource(R.dimen.filter_section_padding)


    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            modifier = Modifier.padding(start = padding, bottom = padding),
            text = filterSectionState.title,
            color = colorResource(R.color.black),
            fontSize = fontsize
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = padding),
            horizontalArrangement = Arrangement.spacedBy(space = spase)
        ) {
            itemsIndexed(filterSectionState.id) { index, optionId ->
                val isSelectedFilter = optionId == filterSectionState.selectedOptionId
                val optionText = filterSectionState.options.getOrNull(index) ?: ""

                FilterOption(
                    filterOptionState = FilterOptionState(
                        text = optionText,
                        id = optionId,
                        isSelected = isSelectedFilter
                    ),
                    filterCondition = filterCondition
                )
            }
        }
    }
}
