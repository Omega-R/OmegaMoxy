package com.omegar.mvp

import kotlin.time.Duration


@InjectViewState
class MoxyPresenter : BasePresenter<MoxyView>() {

    init {
        viewState.showToast("Hello World!")
    }

    var duration: Duration
        get() = TODO("Not yet implemented")
        set(value) {}

    fun test(count: Int) {
        TODO("Not yet implemented")
    }


}