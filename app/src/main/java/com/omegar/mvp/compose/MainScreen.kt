package com.omegar.mvp.compose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun MainScreen() {
    var textColor by remember { mutableStateOf(Color.Black) }

    val state = providePresenterState(factory = { ComposePresenter() }, randomColorBlock = {
        textColor = Color(
            red = (0..255).random(),
            green = (0..255).random(),
            blue = (0..255).random()
        )
    })

    val loading = state.loadingFlow.collectAsState(initial = false).value

    state.presenter.requestChangeColor()

    Text(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentHeight(Alignment.CenterVertically),
        textAlign = TextAlign.Center,
        color = textColor,
        text = "loading = $loading"
    )
}