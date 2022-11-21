package com.omegar.mvp

import android.os.SystemClock
import com.omegar.mvp.MoxyActivity.Companion

class MoxyPresenter: MvpPresenter<MoxyView>() {

    init {
        println("TestAnt: init presenter " + (SystemClock.elapsedRealtime() - MoxyActivity.time))

        viewState.test()
    }

    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        println("TestAnt: onFirstViewAttach presenter " + (SystemClock.elapsedRealtime() - MoxyActivity.time))
    }

}