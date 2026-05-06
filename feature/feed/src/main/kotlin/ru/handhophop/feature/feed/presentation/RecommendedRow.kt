package ru.handhophop.feature.feed.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.handhophop.feature.feed.R
import ru.handhophop.design.R as DesignR

@Composable
internal fun RecommendedRow(
    state: FeedUiState.Success,
    onPhotoClicked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = dimensionResource(DesignR.dimen.feed_spacing)
    Column(modifier = modifier.fillMaxWidth().padding(
        top = dimensionResource(DesignR.dimen.text_spacing)
    )) {
        Text(
            modifier = modifier
                .padding(
                    start = dimensionResource(DesignR.dimen.feed_spacing)
                ),
            text = stringResource(R.string.recommended_row_header),
            fontWeight = FontWeight.SemiBold,
            fontSize = dimensionResource(DesignR.dimen.recommended_header).value.sp,
        )

        Spacer(modifier = Modifier.height(spacing))

        if (state.isRecommendedLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = spacing,
                        end = spacing
                    ),
                    horizontalArrangement = Arrangement.spacedBy(dimensionResource(DesignR.dimen.recommended_row_spacing))
                ) {
                    items(items = state.recommendedPhotos, key = {it.id}) { photo ->
                        AsyncImage(
                            model = photo.photoUrl,
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {onPhotoClicked(photo.photoUrl)},
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}
