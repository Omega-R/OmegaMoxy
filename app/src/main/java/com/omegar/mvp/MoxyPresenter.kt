package com.omegar.mvp

import com.omegar.mvp.base.BasePresenter
import kotlin.time.Duration

class MoxyPresenter<E> : BasePresenter<Long, Int, MoxyView<Int, E>>() {

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