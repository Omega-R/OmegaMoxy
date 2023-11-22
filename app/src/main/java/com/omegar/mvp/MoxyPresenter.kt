package com.omegar.mvp

import kotlin.time.Duration

class MoxyPresenter : BasePresenter<Long, MoxyView>() {

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