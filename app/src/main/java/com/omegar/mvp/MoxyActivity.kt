package com.omegar.mvp

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Toast
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MoxyActivity: MvpAppCompatActivity(R.layout.activity_moxy), MoxyView {

    companion object {
        var first: Boolean = true
    }

    private val presenter: MoxyPresenter by providePresenter()

    override var duration: Duration = 0.seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.test, MoxyFragment())
                .commit()
        }
    }

    override fun test() {

    }

}