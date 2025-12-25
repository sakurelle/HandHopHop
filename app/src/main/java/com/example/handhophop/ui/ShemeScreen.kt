package com.example.handhophop.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.handhophop.R

@Composable
fun ShemeScreen(
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

            CenterContentScheme(imageUrl = selectedUrl)

            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ColumnScope.CenterContentScheme(imageUrl: String?) {
    val sidePad = dimensionResource(id = R.dimen.screen_side_padding)

    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .padding(horizontal = sidePad),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.center_block_gap))
        ) {
            SchemePreviewBlock(imageUrl = imageUrl)
            DownloadButton()
            ColorPalette()
        }
    }
}

@Composable
private fun SchemePreviewBlock(imageUrl: String?) {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)
    val outerColor = colorResource(R.color.card_beige)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = pad),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        if (imageUrl != null) {
            // ✅ картинка из API — показываем без обрезки по ширине
            AsyncImage(
                model = imageUrl,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // ✅ дефолтная картинка — сохраняем aspectRatio (как раньше)
            val painter = painterResource(R.drawable.project_preview)
            val intrinsic: Size = painter.intrinsicSize
            val aspect = if (intrinsic.width > 0f && intrinsic.height > 0f) {
                intrinsic.width / intrinsic.height
            } else 1f

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
}

@Composable
private fun DownloadButton() {
    val h = dimensionResource(R.dimen.stats_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)

    val bg = colorResource(R.color.primary_brown)
    val textWhite = Color.White

    Button(
        onClick = { /* TODO: реализовать скачивание */ },
        modifier = Modifier
            .fillMaxWidth()
            .height(h),
        shape = RoundedCornerShape(r),
        colors = ButtonDefaults.buttonColors(containerColor = bg),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = elevation0)
    ) {
        Text(
            text = stringResource(R.string.download_scheme),
            color = textWhite,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ColorPalette() {
    val titleColor = colorResource(R.color.text_dark)
    val gap = 8.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap)
    ) {
        Text(
            text = stringResource(R.string.color_palette),
            color = titleColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            for (i in 1..10) {
                ColorSwatch(color = getColorForIndex(i), number = i)
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, number: Int) {
    val size = 32.dp
    val fontSize = 10.sp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color, RoundedCornerShape(4.dp))
        )
        Text(
            text = number.toString(),
            fontSize = fontSize,
            color = Color.Black.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun getColorForIndex(index: Int): Color {
    return when (index) {
        1 -> Color(0xFFA5D6A7)
        2 -> Color(0xFF90CAF9)
        3 -> Color(0xFFCE93D8)
        4 -> Color(0xFFFFCC80)
        5 -> Color(0xFFEF9A9A)
        6 -> Color(0xFFBDBDBD)
        7 -> Color(0xFF64B5F6)
        8 -> Color(0xFF4DB6AC)
        9 -> Color(0xFF8D6E63)
        10 -> Color(0xFF795548)
        else -> Color.Gray
    }
}
