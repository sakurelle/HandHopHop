package ru.handhophop.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import ru.handhophop.core.design.BackgroundPattern
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.design.R as DesignR

@Composable
internal fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = HandHopHopDesignSystem.colors

    val screenPadding = dimensionResource(DesignR.dimen.profile_screen_padding)
    val contentSpacing = dimensionResource(DesignR.dimen.profile_content_spacing)
    val avatarSize = dimensionResource(DesignR.dimen.settings_profile_avatar_size)
    val cardCornerRadius = dimensionResource(DesignR.dimen.profile_card_corner_radius)
    val cardElevation = dimensionResource(DesignR.dimen.profile_card_elevation)
    val cardInnerPadding = dimensionResource(DesignR.dimen.profile_card_inner_padding)
    val infoSpacing = dimensionResource(DesignR.dimen.profile_info_spacing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        BackgroundPattern()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            AsyncImage(
                model = uiState.avatarUrl,
                contentDescription = stringResource(R.string.profile_avatar_content_description),
                modifier = Modifier
                    .size(avatarSize)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )

                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(DesignR.dimen.profile_name_spacing)
                    )
                )

                Text(
                    text = uiState.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textSecondary
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = colors.surfaceSoft
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(infoSpacing)
                ) {
                    SettingsInfoItem(
                        title = stringResource(R.string.profile_email_label),
                        value = uiState.email
                    )
                    SettingsInfoItem(
                        title = stringResource(R.string.profile_phone_label),
                        value = uiState.phone
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsInfoItem(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = HandHopHopDesignSystem.colors.textSecondary.copy(alpha = 0.8f)
        )

        Spacer(
            modifier = Modifier.height(
                dimensionResource(DesignR.dimen.profile_info_title_spacing)
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = HandHopHopDesignSystem.colors.textPrimary
        )
    }
}
