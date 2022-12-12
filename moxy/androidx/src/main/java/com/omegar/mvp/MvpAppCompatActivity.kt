package com.omegar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.annotation.ContentView
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity

/**
 * Date: 25-July-18
 * Time: 2:51
 *
 * @author Vova Stelmashchuk
 */
open class MvpAppCompatActivity : AppCompatActivity, MvpDelegateHolder {

    companion object {

        private const val EXTRA_UNIQUE_KEY = "MVP_UNIQUE_KEY"
        private var lastStartIntent: Intent? = null


        internal fun updateLastStartIntent(intent: Intent) {
            if (!intent.hasExtra(EXTRA_UNIQUE_KEY)) {
                intent.putExtra(EXTRA_UNIQUE_KEY, System.identityHashCode(intent))
            }
            lastStartIntent = intent
        }

    }

    private val mvpDelegate: MvpDelegate<out MvpAppCompatActivity> = MvpDelegate(this)

    @Suppress("unused")
    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    init {
        if (lastStartIntent?.component?.className == this::class.java.name) {
            intent = lastStartIntent
            mvpDelegate.uniqueKey = intent.getIntExtra(EXTRA_UNIQUE_KEY, mvpDelegate.uniqueKey)
            mvpDelegate.enableAutoCreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uniqueKey = intent.getIntExtra(EXTRA_UNIQUE_KEY, 0)
        when {
            uniqueKey == 0 -> {
                intent.putExtra(EXTRA_UNIQUE_KEY, mvpDelegate.uniqueKey)
            }
            uniqueKey != mvpDelegate.uniqueKey -> {
                mvpDelegate.uniqueKey = uniqueKey
            }
        }
        mvpDelegate.onCreate(savedInstanceState?.toKeyStore())
    }

    override fun onStart() {
        super.onStart()
        mvpDelegate.onAttach()
    }

    override fun onResume() {
        super.onResume()
        mvpDelegate.onAttach()
    }

    @Suppress("DEPRECATION")
    override fun startActivityForResult(intent: Intent, requestCode: Int, options: Bundle?) {
        updateLastStartIntent(intent)
        super.startActivityForResult(intent, requestCode, options)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mvpDelegate.onSaveInstanceState(outState.toKeyStore())
        mvpDelegate.onDetach()
    }

    override fun onStop() {
        super.onStop()
        mvpDelegate.onDetach()
    }

    override fun onDestroy() {
        super.onDestroy()
        mvpDelegate.onDestroyView()
        if (isFinishing) {
            mvpDelegate.onDestroy()
        }
    }

    /**
     * @return The [MvpDelegate] being used by this Activity.
     */
    override fun getMvpDelegate(): MvpDelegate<*> = mvpDelegate
}