package ru.handhophop.feature.mash.complexity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ComplexityScreen(
    selectedComplexity: ComplexityType,
    onComplexitySelected: (ComplexityType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = "Выберите сложность:",
            style = MaterialTheme.typography.bodyLarge,
        )

        Spacer(modifier = Modifier.height(8.dp))

        ComplexityType.entries.forEach { complexity ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onComplexitySelected(complexity) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    selected = selectedComplexity == complexity,
                    onClick = { onComplexitySelected(complexity) },
                )

                Text(
                    text = "${complexity.title} (${complexity.colorsCount} цветов)",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}