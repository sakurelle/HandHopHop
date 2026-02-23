package com.example.handhophop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.handhophop.R

@Composable
fun BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val bottomBarBackground = colorResource(R.color.bottom_bar)
    val buttonBackground = colorResource(R.color.button)
    val whiteColor = colorResource(R.color.white)
    val colorButPlus = colorResource(R.color.plus_button)
    val radius = dimensionResource(R.dimen.main_radius)
    val plusSize = dimensionResource(R.dimen.plus_size)
    // plusPadding больше не нужен для смещения, но может быть полезен для других отступов
    // val plusPadding = dimensionResource(R.dimen.plus_padding)
    val barHeight = dimensionResource(R.dimen.bottom_bar_height)
    val bottomPadding = dimensionResource(R.dimen.bottom_pading)


    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),

        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier,
            color = bottomBarBackground,
            shape = RoundedCornerShape(topStart = radius, topEnd = radius)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(
                        bottom =bottomPadding),
                // ИЗМЕНЕНО: Выравниваем все элементы по верху, чтобы они были на одной линии
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                NavItem(
                    title = stringResource(R.string.home),
                    icon = painterResource(R.drawable.home),
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) { launchSingleTop = true } },
                    activeColor = whiteColor,
                    inactiveColor = buttonBackground,
                    activeContainerColor = buttonBackground
                )

                NavItem(
                    title = stringResource(R.string.online_sheme),
                    icon = painterResource(R.drawable.online),
                    selected = currentRoute == Screen.OnlineSchemes.route,
                    onClick = { navController.navigate(Screen.OnlineSchemes.route) { launchSingleTop = true } },
                    activeColor = whiteColor,
                    inactiveColor = buttonBackground,
                    activeContainerColor = buttonBackground
                )

                // Пустое место для центральной кнопки
                Spacer(Modifier.weight(1f))

                NavItem(
                    title = stringResource(R.string.download_sheme),
                    icon = painterResource(R.drawable.download_sheme),
                    selected = currentRoute == "downloads_route",
                    onClick = { /* navController.navigate(...) */ },
                    activeColor = whiteColor,
                    inactiveColor = buttonBackground,
                    activeContainerColor = buttonBackground
                )

                NavItem(
                    title = stringResource(R.string.profile),
                    icon = painterResource(R.drawable.profile),
                    selected = currentRoute == Screen.Profile.route,
                    onClick = { navController.navigate(Screen.Profile.route) { launchSingleTop = true } },
                    activeColor = whiteColor,
                    inactiveColor = buttonBackground,
                    activeContainerColor = buttonBackground
                )
            }
        }

        // --- ИЗМЕНЕНО: Кнопка "Плюс" теперь выровнена по центру всего Box ---
        Button(
            modifier = Modifier
                .align(Alignment.Center) // Просто выравниваем по центру
                .size(plusSize),
            shape = RoundedCornerShape(radius),
            elevation = null,
            onClick = { /*TODO*/ },
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.button)
            )
        ) {
            Icon(
                painter = painterResource(R.drawable.plus),
                contentDescription = null,
                tint = whiteColor
            )
        }
    }
}

@Composable
private fun RowScope.NavItem(
    title: String,
    icon: Painter,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color,
    inactiveColor: Color,
    activeContainerColor: Color,
) {
    val containerColor = if (selected) activeContainerColor else Color.Transparent
    val contentColor = if (selected) activeColor else inactiveColor
    val buttonTextSize = dimensionResource(R.dimen.button_text)
    // val bottomPadding = dimensionResource(R.dimen.bottom_pading)
    val radius = dimensionResource(R.dimen.main_radius)
    // Высота элемента, чтобы все были одинаковыми
    val itemHeight = dimensionResource(R.dimen.bottom_bar_height)


    Column(
        modifier = Modifier
            .weight(1f)
            .wrapContentHeight()
            .padding(
                top = dimensionResource(R.dimen.top_bar_padding)
            ),

        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center // Центрируем контент внутри элемента
    ) {
        Button(
            onClick = onClick,
            shape = RoundedCornerShape(radius),
            contentPadding = PaddingValues(0.dp),
            elevation = null,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColor
            )
        ) {
            Icon(
                painter = icon,
                contentDescription = title,
            )
        }
        Text(
            text = title,
            color = inactiveColor,
            fontSize = buttonTextSize.value.sp,
            textAlign = TextAlign.Center,
           // modifier = Modifier.padding(top = 4.dp)
        )
    }
}
