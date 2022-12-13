package com.omegar.mvp

import android.content.Intent
import com.omegar.mvp.ktx.providePresenter
import com.omegar.mvp.presenter.InjectPresenter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MoxyFragment : MvpAppCompatFragment(R.layout.activity_moxy), MoxyView {

    @InjectPresenter
    lateinit var presenter: MoxyPresenter

    override var duration: Duration = 0.seconds

    override fun test() {
        if (MoxyActivity.first) {
            MoxyActivity.first = false
            startActivity(Intent(context, MoxyActivity::class.java))
        }
    }


}