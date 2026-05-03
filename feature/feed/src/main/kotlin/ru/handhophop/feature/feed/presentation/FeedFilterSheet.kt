package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ru.handhophop.design.R

@Composable
internal fun FeedFilterSheet(
    sections: List<FilterSectionState>,
    onOptionSelected: (sectionId: Int, optionId: Int) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(
                interactionSource = null,
                indication = null,
                onClick = {}
            )
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        sections.forEach { section ->
            FilterSection(
                filterSectionState = section,
                filterCondition = { optionId ->
                    onOptionSelected(section.sectionId, optionId)
                }
            )
        }

        IconButton(
            onClick = onDismiss,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arow_vertic),
                contentDescription = "свернуть",
                tint = colorResource(R.color.black)
            )
        }
    }
}

