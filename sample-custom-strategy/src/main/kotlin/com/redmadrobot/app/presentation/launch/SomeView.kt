package com.redmadrobot.app.presentation.launch

import com.omegar.mvp.MvpView

interface SomeView : MvpView {
    fun toggleCheese(enable: Boolean)
}