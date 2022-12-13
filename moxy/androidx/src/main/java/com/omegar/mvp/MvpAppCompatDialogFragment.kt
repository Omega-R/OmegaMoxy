package com.omegar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatDialogFragment

open class MvpAppCompatDialogFragment : AppCompatDialogFragment, MvpDelegateHolder<MvpAppCompatDialogFragment> {

    companion object {

        private const val KEY_UNIQUE_KEY = "UNIQUE_KEY"
    }

    private var stateSaved = false
    @Suppress("LeakingThis")
    final override val mvpDelegate = MvpDelegate(this)

    constructor() : super()

    constructor(@LayoutRes contentLayoutId: Int) : super(contentLayoutId)

    override fun setArguments(args: Bundle?) {
        val arguments = args ?: Bundle()
        if (!arguments.containsKey(KEY_UNIQUE_KEY)) {
            arguments.putInt(KEY_UNIQUE_KEY, mvpDelegate.uniqueKey)
        } else {
            mvpDelegate.uniqueKey = arguments.getInt(KEY_UNIQUE_KEY)
        }

        super.setArguments(args)
        mvpDelegate.autoCreate()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mvpDelegate.onCreate(savedInstanceState?.toKeyStore())
    }

    override fun onResume() {
        super.onResume()
        stateSaved = false
        mvpDelegate.onAttach()
    }

    override fun startActivity(intent: Intent, options: Bundle?) {
        MvpAppCompatActivity.updateLastStartIntent(intent)
        super.startActivity(intent, options)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        MvpAppCompatActivity.updateLastStartIntent(intent)
        super.startActivityForResult(intent, requestCode)
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
        if (isStateSaved) {
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