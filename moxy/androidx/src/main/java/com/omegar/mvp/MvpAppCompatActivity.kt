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
open class MvpAppCompatActivity : AppCompatActivity, MvpDelegateHolder<MvpAppCompatActivity> {

    final override val mvpDelegate by lazy(mode = LazyThreadSafetyMode.NONE) { MvpDelegate(this) }

    @Suppress("unused")
    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mvpDelegate.onCreate(savedInstanceState?.toKeyStore() ?: intent.extras?.toKeyStore())
    }

    override fun onStart() {
        super.onStart()
        mvpDelegate.onAttach()
    }

    override fun onResume() {
        super.onResume()
        mvpDelegate.onAttach()
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

}