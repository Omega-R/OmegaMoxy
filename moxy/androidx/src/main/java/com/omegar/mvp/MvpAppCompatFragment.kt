package com.omegar.mvp

import android.content.Intent
import androidx.annotation.LayoutRes
import android.os.Bundle
import androidx.annotation.ContentView
import androidx.fragment.app.Fragment

/**
 * Date: 25-July-18
 * Time: 4:43
 *
 * @author Vova Stelmashchuk
 */
open class MvpAppCompatFragment : Fragment, MvpDelegateHolder<MvpAppCompatFragment> {

    private var stateSaved = false
    @Suppress("LeakingThis")
    final override val mvpDelegate = MvpDelegate(this)

    constructor() : super()

    @ContentView
    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvpDelegate.onCreate(savedInstanceState?.toKeyStore() ?: arguments?.toKeyStore())
    }

    override fun onStart() {
        super.onStart()
        stateSaved = false
        mvpDelegate.onAttach()
    }

    override fun onResume() {
        super.onResume()
        stateSaved = false
        mvpDelegate.onAttach()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stateSaved = true
        mvpDelegate.onSaveInstanceState(outState.toKeyStore())
        mvpDelegate.onDetach()
    }

    override fun onStop() {
        super.onStop()
        mvpDelegate.onDetach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mvpDelegate.onDetach()
        mvpDelegate.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()

        //We leave the screen and respectively all fragments will be destroyed
        if (requireActivity().isFinishing) {
            mvpDelegate.onDestroy()
            return
        }

        // When we rotate device isRemoving() return true for fragment placed in backstack
        // http://stackoverflow.com/questions/34649126/fragment-back-stack-and-isremoving
        if (stateSaved) {
            stateSaved = false
            return
        }

        // See https://github.com/Arello-Mobile/Moxy/issues/24
        var anyParentIsRemoving = false
        var parent = parentFragment
        while (!anyParentIsRemoving && parent != null) {
            anyParentIsRemoving = parent.isRemoving
            parent = parent.parentFragment
        }
        if (isRemoving || anyParentIsRemoving) {
            mvpDelegate.onDestroy()
        }
    }

}