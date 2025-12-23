package com.example.handhophop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add

import androidx.compose.material.icons.outlined.Home

import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.example.handhophop.R

import androidx.compose.foundation.layout.ColumnScope


@Composable
fun HomeScreen() {
    val bg = colorResource(R.color.bg_beige)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        BackgroundPattern()

        Column(modifier = Modifier.fillMaxSize()) {
            TopBanner()

            CenterContent()

            BottomBar(modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun BackgroundPattern() {
    // Если bg_pattern отсутствует — закомментируй этот composable целиком
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
private fun TopBanner() {
    val h = dimensionResource(R.dimen.top_banner_height)
    val bottomPad = dimensionResource(R.dimen.top_banner_bottom_padding)

    val radius0 = dimensionResource(R.dimen.radius_0)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val bannerColor = colorResource(R.color.card_beige)
    val textColor = colorResource(R.color.text_dark)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(h),
        shape = RoundedCornerShape(radius0),
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
                text = stringResource(R.string.home_title_incomplete),
                color = textColor,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ColumnScope.CenterContent() {
    val sidePad = dimensionResource(id = R.dimen.screen_side_padding)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = sidePad),
        contentAlignment = Alignment.Center
    ) {
        CenterStack3Blocks()
    }
}

@Composable
private fun CenterStack3Blocks() {
    val w = dimensionResource(R.dimen.center_block_width)
    val gap = dimensionResource(R.dimen.center_block_gap)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        PreviewBlock(modifier = Modifier.width(w))
        StatsButtonBlock(modifier = Modifier.width(w))
        StartButtonBlock(modifier = Modifier.width(w))
    }
}

@Composable
private fun PreviewBlock(modifier: Modifier = Modifier) {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val padH = dimensionResource(R.dimen.preview_text_h_padding)
    val padV = dimensionResource(R.dimen.preview_text_v_padding)

    val outerColor = colorResource(R.color.card_beige)
    val innerColor = colorResource(R.color.card_beige_2)
    val textDark = colorResource(R.color.text_dark)
    val textMuted = colorResource(R.color.text_muted)

    val painter = painterResource(R.drawable.project_preview)
    val intrinsic: Size = painter.intrinsicSize
    val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f) intrinsic.width / intrinsic.height else 1f

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
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(aspect)
                )
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
private fun StatsButtonBlock(modifier: Modifier = Modifier) {
    val h = dimensionResource(R.dimen.stats_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val hPad = dimensionResource(R.dimen.button_h_padding)

    val bg = colorResource(R.color.card_beige_2)
    val textDark = colorResource(R.color.text_dark)
    val textMuted = colorResource(R.color.text_muted)

    Card(
        modifier = modifier.height(h),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_statistics),
                color = textDark,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.Person,
                contentDescription = null,
                tint = textMuted
            )
        }
    }
}

@Composable
private fun StartButtonBlock(modifier: Modifier = Modifier) {
    val h = dimensionResource(R.dimen.start_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val bg = colorResource(R.color.primary_brown)

    Button(
        onClick = { /* фиктивно */ },
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

private enum class BottomItem {
    Home, Online, Add, Downloads, Profile
}

@Composable
private fun BottomBar(modifier: Modifier = Modifier) {
    val navBg = colorResource(R.color.nav_bg)
    val icon = colorResource(R.color.nav_icon)
    val iconActive = colorResource(R.color.nav_icon_active)
    val fabBg = colorResource(R.color.fab_bg)

    val barH = dimensionResource(R.dimen.bottom_bar_height)
    val itemW = dimensionResource(R.dimen.bottom_item_width)
    val centerGap = dimensionResource(R.dimen.center_gap_width)

    val fabSize = dimensionResource(R.dimen.fab_size)
    val fabLift = dimensionResource(R.dimen.fab_lift)

    val iconBtnSize = dimensionResource(R.dimen.nav_icon_button_size)

    var selected by remember { mutableStateOf(BottomItem.Home) }

    Box(modifier = modifier) {
        Surface(color = navBg) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barH),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                NavItem(
                    title = stringResource(R.string.nav_home),
                    icon = Icons.Outlined.Home,
                    selected = selected == BottomItem.Home,
                    onClick = { selected = BottomItem.Home },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                NavItem(
                    title = stringResource(R.string.nav_online),
                    icon = Icons.Outlined.Person,
                    selected = selected == BottomItem.Online,
                    onClick = { selected = BottomItem.Online },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                Spacer(modifier = Modifier.width(centerGap))

                NavItem(
                    title = stringResource(R.string.nav_downloads),
                    icon = Icons.Outlined.Person,
                    selected = selected == BottomItem.Downloads,
                    onClick = { selected = BottomItem.Downloads },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                NavItem(
                    title = stringResource(R.string.nav_profile),
                    icon = Icons.Outlined.Person,
                    selected = selected == BottomItem.Profile,
                    onClick = { selected = BottomItem.Profile },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )
            }
        }

        FloatingActionButton(
            onClick = { selected = BottomItem.Add },
            containerColor = fabBg,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = -fabLift)
                .size(fabSize)
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = stringResource(R.string.nav_add)
            )
        }
    }
}

@Composable
private fun NavItem(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    width: androidx.compose.ui.unit.Dp,
    iconButtonSize: androidx.compose.ui.unit.Dp
) {
    val tint = if (selected) activeColor else inactiveColor

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(iconButtonSize)) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Text(
            text = title,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2
        )
    }
}
