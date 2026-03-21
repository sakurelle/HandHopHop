package ru.handhophop.feature.profile

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage

private const val PROFILE_NAME = "Егор Иванов"
private const val PROFILE_NICKNAME = "@abober4000"
private const val PROFILE_EMAIL = "egor@example.com"
private const val PROFILE_PHONE = "+7 (999) 123-45-67"
private const val PROFILE_AVATAR_URL = "Я хз че сюда, наверное ссылку"

@Composable
internal fun ProfileScreen() {
    val uiState = ProfileUiState(
        name = PROFILE_NAME,
        nickname = PROFILE_NICKNAME,
        email = PROFILE_EMAIL,
        phone = PROFILE_PHONE,
        avatarUri = PROFILE_AVATAR_URL
    )

    val screenPadding = dimensionResource(R.dimen.profile_screen_padding)
    val contentSpacing = dimensionResource(R.dimen.profile_content_spacing)
    val avatarSize = dimensionResource(R.dimen.profile_avatar_size)
    val cardCornerRadius = dimensionResource(R.dimen.profile_card_corner_radius)
    val cardElevation = dimensionResource(R.dimen.profile_card_elevation)
    val cardInnerPadding = dimensionResource(R.dimen.profile_card_inner_padding)
    val infoSpacing = dimensionResource(R.dimen.profile_info_spacing)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.profile_background))
            .padding(screenPadding)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            AsyncImage(
                model = uiState.avatarUri,
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
                    color = colorResource(R.color.profile_primary_text)
                )

                Spacer(
                    modifier = Modifier.height(
                        dimensionResource(R.dimen.profile_name_spacing)
                    )
                )

                Text(
                    text = uiState.nickname,
                    style = MaterialTheme.typography.bodyLarge,
                    color = colorResource(R.color.profile_secondary_text)
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(cardCornerRadius),
                colors = CardDefaults.cardColors(
                    containerColor = colorResource(R.color.profile_card_background)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(cardInnerPadding),
                    verticalArrangement = Arrangement.spacedBy(infoSpacing)
                ) {
                    ProfileInfoItem(
                        title = stringResource(R.string.profile_email_label),
                        value = uiState.email
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileInfoItem(
    title: String,
    value: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = colorResource(R.color.profile_hint_text)
        )

        Spacer(
            modifier = Modifier.height(
                dimensionResource(R.dimen.profile_info_title_spacing)
            )
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colorResource(R.color.profile_primary_text)
        )
    }
}