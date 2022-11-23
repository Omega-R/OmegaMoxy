package com.omegar.mvp

import android.os.Bundle
import android.os.SystemClock
import com.omegar.mvp.MoxyActivity.Companion
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MoxyFragment : MvpAppCompatFragment(R.layout.activity_moxy), MoxyView {

    private val presenter: MoxyPresenter by providePresenter()

    override var duration: Duration = 0.seconds

    override fun test() {

    }


}