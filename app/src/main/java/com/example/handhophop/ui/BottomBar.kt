package com.example.handhophop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.handhophop.R

private enum class BottomItem {
    Home, Online, Add, Downloads, Profile
}

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBg = colorResource(R.color.bar)
    val icon = colorResource(R.color.nav_icon)
    val iconActive = colorResource(R.color.bg_beige)
    val fabBg = colorResource(R.color.fab_bg)

    val barH = dimensionResource(R.dimen.bottom_bar_height)
    val itemW = dimensionResource(R.dimen.bottom_item_width)
    val centerGap = dimensionResource(R.dimen.center_gap_width)

    val fabSize = dimensionResource(R.dimen.fab_size)
    val fabLift = dimensionResource(R.dimen.fab_lift)
    val iconBtnSize = dimensionResource(R.dimen.nav_icon_button_size)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val selected = when (currentRoute) {
        Screen.Home.route -> BottomItem.Home
        Screen.OnlineSchemes.route -> BottomItem.Online
        Screen.Profile.route -> BottomItem.Profile
        else -> BottomItem.Home
    }

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
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                NavItem(
                    title = stringResource(R.string.nav_online),
                    icon = Icons.Outlined.Search,
                    selected = selected == BottomItem.Online,
                    onClick = {
                        navController.navigate(Screen.OnlineSchemes.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                Spacer(modifier = Modifier.width(centerGap))

                NavItem(
                    title = stringResource(R.string.nav_downloads),
                    icon = Icons.Outlined.Done,
                    selected = false,
                    onClick = { /* TODO */ },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )

                NavItem(
                    title = stringResource(R.string.nav_profile),
                    icon = Icons.Outlined.Person,
                    selected = selected == BottomItem.Profile,
                    onClick = {
                        navController.navigate(Screen.Profile.route) {
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    activeColor = iconActive,
                    inactiveColor = icon,
                    width = itemW,
                    iconButtonSize = iconBtnSize
                )
            }
        }

        FloatingActionButton(
            onClick = { /* TODO */ },
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

    val indicatorColor = colorResource(R.color.nav_selected_indicator)
    val indW = dimensionResource(R.dimen.nav_selected_indicator_width)
    val indH = dimensionResource(R.dimen.nav_selected_indicator_height)
    val indR = dimensionResource(R.dimen.nav_selected_indicator_radius)
    val indGap = dimensionResource(R.dimen.nav_selected_indicator_gap_under_icon)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(top = dimensionResource(R.dimen.nav_item_top_padding))
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            IconButton(onClick = onClick, modifier = Modifier.size(iconButtonSize)) {
                Icon(imageVector = icon, contentDescription = null, tint = tint)
            }

            Spacer(modifier = Modifier.height(indGap + indH))
        }

        Spacer(modifier = Modifier.height(dimensionResource(R.dimen.nav_icon_text_gap)))

        Text(
            text = title,
            color = tint,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 2,
            textAlign = TextAlign.Center
        )
    }
}
