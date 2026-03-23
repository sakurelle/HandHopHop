package com.example.handhophop.feature.mash.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal fun ScreenBase(
    state: ScreenState,
    onNavigate: (ScreenState) -> Unit,
    mainScreen: @Composable () -> Unit,
    topBarState: TopBarState
){
    //val mainPadding = dimensionResource(R.dimen.main_padding)


    Box(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        BackgroundPattern()

        //основной блок
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(topBarState)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    //.padding(mainPadding)
            ) {
                mainScreen()
            }
            BottomBar(
                onNavigate = onNavigate,
                state = state
            )
        }
    }
}
