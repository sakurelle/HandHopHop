package ru.handhophop.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.sp
import ru.handhophop.design.R

@Composable
fun TopBar(state: TopBarState){
    //dimen
    val height = dimensionResource(R.dimen.top_bar_height)
    val radius = dimensionResource(R.dimen.main_radius)
    val topBarStr = dimensionResource(R.dimen.top_bar_str).value.sp
    val bottomPadding = dimensionResource(R.dimen.top_bar_padding)


    //Color
    val topBarBackground = colorResource(R.color.main_color)
    val textColor =colorResource(R.color.button)

    Box(
        modifier = Modifier
            .height(height)
            .fillMaxWidth()
            .background(
                topBarBackground,
                shape = RoundedCornerShape(
                    bottomEnd = radius,
                    bottomStart = radius
                )
            ),
        contentAlignment = Alignment.BottomCenter
    ){
        Text(
            modifier = Modifier
                .padding(
                    bottom = bottomPadding
                ),
            text = state.topBarTitle,
            color = textColor,
            fontSize = topBarStr
        )
    }
}