package ru.handhophop.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import ru.handhophop.R

@Composable
fun ProfilePhotoScreen(
    navController: NavHostController,
    vm: ProfileViewModel
) {
    val bg = colorResource(R.color.bg_beige)
    val state by vm.state.collectAsState()

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) vm.setAvatarFromPicker(uri)
    }

    Box(Modifier.fillMaxSize().background(bg)) {
        BackgroundPattern()

        Column(Modifier.fillMaxSize()) {
            TopBanner(title = stringResource(R.string.profile_title))

            PhotoCenter(
                avatarUri = state.avatarUri,
                onPick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onDone = { navController.popBackStack() }
            )

            BottomBar(navController = navController, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun ColumnScope.PhotoCenter(
    avatarUri: String?,
    onPick: () -> Unit,
    onDone: () -> Unit
) {
    val w = dimensionResource(R.dimen.photo_buttons_stack_width)
    val gap = dimensionResource(R.dimen.center_block_gap)

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PhotoPreviewCard(avatarUri = avatarUri)

        Spacer(Modifier.height(gap))

        FlatCardButton(
            modifier = Modifier.width(w),
            text = stringResource(R.string.pick_from_gallery),
            onClick = onPick
        )

        Spacer(Modifier.height(gap))

        FlatCardButton(
            modifier = Modifier.width(w),
            text = stringResource(R.string.back_gallery),
            onClick = onDone
        )
    }
}

@Composable
private fun PhotoPreviewCard(avatarUri: String?) {
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val sidePad = dimensionResource(R.dimen.photo_card_hpad)
    val outer = colorResource(R.color.card_beige)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = sidePad),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = outer),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        if (avatarUri != null) {
            AsyncImage(
                model = avatarUri,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        } else {
            Image(
                painter = painterResource(R.drawable.project_preview),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        }
    }
}

@Composable
private fun FlatCardButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit
) {
    val h = dimensionResource(R.dimen.stats_button_height)
    val r = dimensionResource(R.dimen.block_radius)
    val elevation0 = dimensionResource(R.dimen.block_elevation)
    val hPad = dimensionResource(R.dimen.button_h_padding)

    val bg = colorResource(R.color.card_beige_2)
    val textDark = colorResource(R.color.text_dark)

    Card(
        modifier = modifier
            .height(h)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(r),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation0)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = hPad),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = textDark,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}