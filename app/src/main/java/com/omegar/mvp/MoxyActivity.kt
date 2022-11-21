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
        var time = SystemClock.elapsedRealtime()
    }

    private val presenter: MoxyPresenter by providePresenter()

    override var duration: Duration = 0.seconds

    init {
        println("TestAnt: init activity " + (SystemClock.elapsedRealtime() - time))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("TestAnt: onCreate activity " + (SystemClock.elapsedRealtime() - time))
    }

    override fun test() {
        Toast.makeText(this, "Test", Toast.LENGTH_LONG).show()
        if (first) {
            first = false
            startActivity(Intent(this, MoxyActivity::class.java))
        }
    }

}