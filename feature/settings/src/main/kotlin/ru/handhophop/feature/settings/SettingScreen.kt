package ru.handhophop.feature.settings

import android.widget.ToggleButton
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onChangeTheme: (isActive: Boolean) -> Unit,
    onClick:()->Unit
) {
    val isChecked = remember { mutableStateOf(false) }

    val checkedThumbColor = colorResource(R.color.profile_background)
    val checkedTrackColor = colorResource(R.color.button)
    val mainColor = colorResource(R.color.main_color)
    val buttonColor = colorResource(R.color.bottom_bar)

    val fontSize = dimensionResource(R.dimen.font_size).value.sp
    val radius = dimensionResource(R.dimen.radius)
    val border = dimensionResource(R.dimen.border)
    val padding = dimensionResource(R.dimen.padding)
    val width = dimensionResource(R.dimen.width)
    val heightBlock = dimensionResource(R.dimen.height_block)
    val heightButton = dimensionResource(R.dimen.height_button)

    val text = stringResource(R.string.dark_theme)

    Column(
        modifier = Modifier
            .wrapContentHeight()
    ) {
        Box(
            modifier = Modifier
                .width(width = width)
                .height(heightBlock)
                .border(
                    border,
                    checkedTrackColor,
                    shape = RoundedCornerShape(radius)
                )
                .background(
                    color = mainColor,
                    shape = RoundedCornerShape(
                        radius
                    )
                ),

            ) {
            Column(
                modifier = Modifier
                    .wrapContentHeight()
                    .padding(
                        padding
                    )
            ) {
                Text(
                    modifier = Modifier
                        .padding(
                            bottom = padding
                        ),
                    text = text,
                    fontSize = fontSize
                )
                Switch(
                    modifier = Modifier,
                    checked = isChecked.value,
                    onCheckedChange = {
                        !isChecked.value
                        onChangeTheme(isChecked.value)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = checkedThumbColor,
                        checkedTrackColor = checkedTrackColor,
                        uncheckedThumbColor = checkedThumbColor,
                        uncheckedTrackColor = checkedTrackColor.copy(0.65f),
                        uncheckedBorderColor = checkedThumbColor.copy(0f)
                    )
                )
            }
        }
        Button(
            modifier = Modifier
                .padding(
                    top = padding
                )
                .border(
                    border,
                    checkedTrackColor,
                    shape = RoundedCornerShape(radius)
                )
                .height(heightButton)

                .width(width = width),
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = buttonColor
            ),
            shape = RoundedCornerShape(radius)
        ) {
            Text(
                text = "Очистить данные",
                fontSize = fontSize,
                color = checkedTrackColor
            )
        }
    }

}
