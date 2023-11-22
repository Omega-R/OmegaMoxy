package com.omegar.mvp

import android.content.Intent
import android.widget.Toast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MoxyFragment : MvpAppCompatFragment(R.layout.activity_moxy), MoxyView<Int> {

    private val presenter: MoxyPresenter by providePresenter {
        MoxyPresenter()
    }

    override var duration: Duration = 0.seconds

    override fun test(count: Int) {
        if (MoxyActivity.first) {
            MoxyActivity.first = false
            startActivity(Intent(context, MoxyActivity::class.java))
        }
    }

    override fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
    }


}