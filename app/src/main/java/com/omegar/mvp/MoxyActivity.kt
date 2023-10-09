package com.omegar.mvp

import android.os.Bundle
import android.widget.Toast
import com.omegar.mvp.ktx.providePresenter
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class MoxyActivity: MvpAppCompatActivity(R.layout.activity_moxy), MoxyView<Int> {

    companion object {
        var first: Boolean = true
    }

    private val presenter: MoxyPresenter by providePresenter {
        MoxyPresenter()
    }

    override var duration: Duration = 0.seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.test, MoxyFragment())
                .commit()
        }
    }

    override fun test(count: Int) {
        Toast.makeText(this, "Test", Toast.LENGTH_LONG).show()
    }

    override fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

}