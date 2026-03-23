package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.handhophop.feature.mash.R
import com.example.handhophop.feature.mash.presentation.ScreenState



@Composable
internal fun BottomBar(
    onNavigate: (ScreenState) -> Unit,
    state: ScreenState
){

    val buttonText = dimensionResource(R.dimen.button_text)
    val bottomPadding = dimensionResource(R.dimen.bottom_pading)
    val radius = dimensionResource(R.dimen.main_radius)

    val bottomBarBackground = colorResource(R.color.bottom_bar)
    val buttonBackground = colorResource(R.color.button)
    val whiteColor = colorResource(R.color.white)
    val colorButPlus = colorResource(R.color.plus_button)



    Box(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth()
            .background(
                bottomBarBackground,
                shape = RoundedCornerShape(
                    topStart = radius,
                    topEnd = radius
                )
            ),
        contentAlignment = Alignment.BottomCenter

    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.Center
        ){
            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        bottom = bottomPadding
                    ),
                horizontalAlignment  = Alignment.CenterHorizontally

            ) {
                Button(
                    modifier = Modifier,
                    elevation = null,
                    shape = RoundedCornerShape(radius),
                    onClick = { onNavigate(ScreenState.HOME) },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(state == ScreenState.HOME) buttonBackground else Color.Transparent
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.home),
                        contentDescription = null,
                        tint = if(state == ScreenState.HOME) whiteColor else buttonBackground
                    )
                }
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.home),
                    color = buttonBackground,
                    fontSize = buttonText.value.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        bottom = bottomPadding
                    ),
                horizontalAlignment  = Alignment.CenterHorizontally

            ) {
                Button(
                    modifier = Modifier,
                    elevation = null,
                    shape = RoundedCornerShape(radius),
                    contentPadding = PaddingValues(0.dp),
                    onClick = { onNavigate(ScreenState.ONLINE)},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(state == ScreenState.ONLINE) buttonBackground else Color.Transparent
                    )
                ) {
                    Icon(
                        modifier = Modifier,
                        painter = painterResource(R.drawable.online),
                        contentDescription = null,
                        tint = if(state == ScreenState.ONLINE) whiteColor else buttonBackground
                    )
                }
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.online_sheme),
                    color = buttonBackground,
                    fontSize = buttonText.value.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        bottom = bottomPadding
                    ),
                horizontalAlignment  = Alignment.CenterHorizontally

            ) {
                Button(
                    modifier = Modifier,
                    elevation = null,
                    shape = RoundedCornerShape(radius),
                    onClick = { onNavigate(ScreenState.DOWNLOAD) },
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(state == ScreenState.DOWNLOAD) buttonBackground else Color.Transparent
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.download_sheme),
                        contentDescription = null,
                        tint = if(state == ScreenState.DOWNLOAD) whiteColor else buttonBackground
                    )
                }
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.download_sheme),
                    color = buttonBackground,
                    fontSize = buttonText.value.sp
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight()
                    .padding(
                        bottom = bottomPadding
                    ),
                horizontalAlignment  = Alignment.CenterHorizontally

            ) {
                Button(
                    modifier = Modifier,
                    elevation = null,
                    shape = RoundedCornerShape(radius),
                    onClick = { onNavigate(ScreenState.PROFILE)},
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if(state == ScreenState.PROFILE) buttonBackground else Color.Transparent
                    )
                ) {
                    Icon(
                        painter = painterResource(R.drawable.profile),
                        contentDescription = null,
                        tint = if(state == ScreenState.PROFILE) whiteColor else buttonBackground
                    )
                }
                Text(
                    modifier = Modifier,
                    textAlign = TextAlign.Center,
                    text = stringResource(R.string.profile),
                    color = buttonBackground,
                    fontSize = buttonText.value.sp
                )
            }
        }

    }
}