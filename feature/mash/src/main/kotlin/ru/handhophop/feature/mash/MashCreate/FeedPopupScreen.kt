package ru.handhophop.feature.mash.MashCreate

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import ru.handhophop.core.design.HandHopHopDesignSystem
import ru.handhophop.design.R

@Composable
fun FeedPopupScreen(
    imageUrl: String?,
    onStartWork: () -> Unit,
    onClose: () -> Unit,
    onSave: (Boolean) -> Unit
) {

    var isSaved by remember { mutableStateOf(false) }
    val colors = HandHopHopDesignSystem.colors

    val mainAccent = colors.bottomBar
    val mainColor = colors.surface
    val buttonColor = colors.primaryAction

    val radius = dimensionResource(R.dimen.main_radius)
    val fontsize = dimensionResource(R.dimen.popup_font_size).value.sp

    val buttonHeight = dimensionResource(R.dimen.popup_button_height)
    val spacerLarge = dimensionResource(R.dimen.popup_spacer_large)
    val spacerSmall = dimensionResource(R.dimen.popup_spacer_small)
    val borderWidth = dimensionResource(R.dimen.popup_button_border)

    Dialog(onDismissRequest = onClose) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(radius),
                colors = CardDefaults.cardColors(containerColor = mainColor.copy(alpha = 0f)),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(0.8f)
                            .clip(RoundedCornerShape(radius))
                    ) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth(),
                            contentScale = ContentScale.Crop
                        )

                        IconButton(
                            onClick = {
                                isSaved = !isSaved
                                onSave(isSaved)
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .background(
                                    color = colors.textPrimary.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(
                                        topStart = radius,
                                        bottomStart = 0.dp,
                                        topEnd = 0.dp,
                                        bottomEnd = radius
                                    )
                                )
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = if (isSaved) R.drawable.active_save else R.drawable.ic_save
                                ),
                                contentDescription = null,
                                tint = colors.onPrimaryAction
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(spacerLarge))

                    Button(
                        onClick = onStartWork,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight),
                        shape = RoundedCornerShape(radius),
                        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
                    ) {
                        Text(
                            text = stringResource(R.string.btn_start_work),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onPrimaryAction,
                            fontSize = fontsize
                        )
                    }

                    Spacer(modifier = Modifier.height(spacerSmall))

                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(buttonHeight)
                            .border(borderWidth, buttonColor, RoundedCornerShape(radius)),
                        shape = RoundedCornerShape(radius),
                        colors = ButtonDefaults.buttonColors(containerColor = mainAccent),
                    ) {
                        Text(
                            text = stringResource(R.string.btn_close),
                            style = MaterialTheme.typography.bodyLarge,
                            fontSize = fontsize,
                            color = buttonColor
                        )
                    }
                }
            }
        }
    }
}
