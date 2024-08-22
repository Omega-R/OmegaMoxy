package com.omegar.mvp.compose

import com.omegar.mvp.MvpView

interface ComposeView: MvpView {

    var loading: Boolean

    fun randomColor()

}