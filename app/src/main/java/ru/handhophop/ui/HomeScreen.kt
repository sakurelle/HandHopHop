package ru.handhophop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ru.handhophop.R

@Composable
fun HomeScreen(
    navController: NavHostController,
    selectedVm: SelectedSchemeViewModel
) {
    val bg = colorResource(R.color.bg_beige)
    val selectedUrl by selectedVm.selectedUrl.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        BackgroundPattern()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBanner(title = stringResource(R.string.home_title_incomplete))
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(
                        top =dimensionResource(R.dimen.main_padding),
                        bottom = dimensionResource(R.dimen.main_padding)
                    )
            ) {
                item{CenterContent(navController, selectedUrl)}
            }

            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun BackgroundPattern() {
    val alpha = runCatching { dimensionResource(R.dimen.bg_pattern_alpha).value }.getOrElse { 0.22f }

    Image(
        painter = painterResource(R.drawable.bg_pattern),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize(),
        alpha = alpha
    )
}

@Composable
fun TopBanner(
    title: String
) {
    val h = dimensionResource(R.dimen.top_bar_height)
    val bottomPad = dimensionResource(R.dimen.top_bar_padding)

    val radius0 = dimensionResource(R.dimen.main_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val bannerColor = colorResource(R.color.card_beige)
    val textColor = colorResource(R.color.button)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(h),
        shape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 0.dp,
            bottomStart = radius0,
            bottomEnd = radius0
        ),
        colors = CardDefaults.cardColors(containerColor = bannerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPad),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = title,
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = dimensionResource(R.dimen.top_bar_str).value.sp
            )
        }
    }
}


@Composable
private fun ColumnScope.CenterContent(
    navController: NavHostController,
    selectedUrl: String?
) {
    val sidePad = dimensionResource(R.dimen.screen_side_padding)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = sidePad),
        contentAlignment = Alignment.Center
    ) {
        CenterStack3Blocks(navController, selectedUrl)
    }
}

@Composable
private fun CenterStack3Blocks(
    navController: NavHostController,
    selectedUrl: String?
) {
    val w = dimensionResource(R.dimen.center_block_width)
    val gap = dimensionResource(R.dimen.center_block_gap)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        PreviewBlock(modifier = Modifier.width(w), imageUrl = selectedUrl)

        StatsButtonBlock(
            modifier = Modifier.width(w),
            onClick = { navController.navigate(Screen.Statistics.route) }
        )

        StartButtonBlock(
            modifier = Modifier.width(w),
            onClick = { navController.navigate(Screen.ShemeScreen.route) }
        )
    }
}

@Composable
private fun PreviewBlock(
    modifier: Modifier = Modifier,
    imageUrl: String?
) {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val padH = dimensionResource(R.dimen.preview_text_h_padding)
    val padV = dimensionResource(R.dimen.preview_text_v_padding)

    val outerColor = colorResource(R.color.card_beige)
    val innerColor = colorResource(R.color.card_beige_2)
    val textDark = colorResource(R.color.text_dark)
    val textMuted = colorResource(R.color.text_muted)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Column {
            Text(
                text = "${stringResource(R.string.home_project_prefix)} ${stringResource(R.string.home_project_name)}",
                color = textDark,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = padH, vertical = padV)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = padH),
                shape = RoundedCornerShape(r),
                colors = CardDefaults.cardColors(containerColor = innerColor),
                elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    val painter = painterResource(R.drawable.project_preview)
                    val intrinsic: Size = painter.intrinsicSize
                    val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f) intrinsic.width / intrinsic.height else 1f

                    Image(
                        painter = painter,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(aspect)
                    )
                }
            }

            Text(
                text = stringResource(R.string.home_only_start),
                color = textMuted,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = padH, vertical = padV)
            )
        }
    }
}

@Composable
private fun StatsButtonBlock(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val h = dimensionResource(R.dimen.stats_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val hPad = dimensionResource(R.dimen.button_h_padding)

    val bg = colorResource(R.color.card_beige_2)
    val textDark = colorResource(R.color.text_dark)

    Button(
        onClick = onClick,
        modifier = modifier.height(h),
        shape = RoundedCornerShape(r),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation0),
        contentPadding = PaddingValues(horizontal = hPad)
    ) {
        Text(
            text = stringResource(R.string.home_statistics),
            color = textDark,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StartButtonBlock(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val h = dimensionResource(R.dimen.start_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val bg = colorResource(R.color.primary_brown)

    Button(
        onClick = onClick,
        modifier = modifier.height(h),
        shape = RoundedCornerShape(r),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation0)
    ) {
        Text(
            text = stringResource(R.string.home_start),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}