package ru.handhophop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import ru.handhophop.R

@Composable
fun StatisticsScreen(navController: NavHostController) {
    val bg = colorResource(R.color.bg_beige)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg)
    ) {
        BackgroundPattern()

        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBanner(title = stringResource(R.string.statistics_title))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(top = dimensionResource(R.dimen.top_padding_statistics)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.top_padding_statistics)),
                contentPadding = PaddingValues(horizontal = dimensionResource(R.dimen.screen_side_padding))
            ) {
                item { ProjectInfoBlock() }
                item { WeeklyStatsChartBlock() }
                item { ProgressCircleBlock() }
                item { ColorUsageListBlock() }
            }

            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ProjectInfoBlock() {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)

    val outerColor = colorResource(R.color.card_beige)
    val textDark = colorResource(R.color.text_dark)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Column(
            modifier = Modifier.padding(pad),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space))
        ) {
            Text(
                text = stringResource(R.string.home_project_prefix)+stringResource(R.string.home_project_name),
                color = textDark,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.quote),
                color = textDark,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WeeklyStatsChartBlock() {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)

    val outerColor = colorResource(R.color.card_beige)
    val textDark = colorResource(R.color.text_dark)
    val barColor = colorResource(R.color.primary_brown)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Column(
            modifier = Modifier.padding(pad),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space))
        ) {
            Text(
                text = "Вы раскрашиваете схему 25 часов",
                color = textDark,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Вы с каждым днём рисуете продуктивнее!",
                color = textDark,
                style = MaterialTheme.typography.bodySmall
            )

            // имитация баров
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .padding(vertical = dimensionResource(R.dimen.space)),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val days = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
                val hours = listOf(3, 4, 2, 5, 3, 4, 4) // заглушка

                for ((day, h) in days.zip(hours)) {
                    Column(
                        modifier = Modifier
                            .width(20.dp)
                            .fillMaxHeight(), // ← занимает всю высоту
                        verticalArrangement  = Arrangement.Bottom // 👈 ВАЖНО: выравниваем по низу
                    ) {
                        Box(
                            modifier = Modifier
                                .width(20.dp)
                                .height((h * 20).dp)
                                .background(barColor, RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = day,
                            fontSize = 10.sp,
                            color = textDark
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProgressCircleBlock() {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)

    val outerColor = colorResource(R.color.card_beige)
    val textDark = colorResource(R.color.text_dark)
    val progressColor = colorResource(R.color.primary_brown)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Row(
            modifier = Modifier
                .padding(pad)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space)/2)
            ) {
                Text(
                    text = "Вы закрасили",
                    color = textDark,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "26 полей",
                    color = textDark,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "из 32 полей",
                    color = textDark,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.Transparent)
            ) {
                CircularProgressIndicator(
                    progress = 0.84f,
                    strokeWidth = dimensionResource(R.dimen.space),
                    modifier = Modifier.fillMaxSize(),
                    color = progressColor
                )
                Text(
                    text = "84%",
                    color = textDark,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
private fun ColorUsageListBlock() {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val pad = dimensionResource(R.dimen.preview_text_h_padding)

    val outerColor = colorResource(R.color.card_beige)
    val textDark = colorResource(R.color.text_dark)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Column(
            modifier = Modifier.padding(pad),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.space))
        ) {
            Text(
                text = "Цвета, которые вы использовали",
                color = textDark,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )

            val colors = listOf(
                Pair(Color(0xFF2196F3), "Голубой — 11 полей"),
                Pair(Color(0xFFFF5722), "Фиолетовый — 5 полей"),
                Pair(Color(0xFFE91E63), "Красный — 3 поля"),
                Pair(Color(0xFF795548), "Апельсиновый — 7 полей")
            )

            for ((color, label) in colors) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = dimensionResource(R.dimen.space)/2),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, RoundedCornerShape(dimensionResource(R.dimen.space)/2))
                    )
                    Spacer(modifier = Modifier.width(dimensionResource(R.dimen.space)))
                    Text(
                        text = label,
                        color = textDark,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}