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
open class MvpAppCompatFragment : Fragment, MvpDelegateHolder {

    companion object {
        private const val KEY_UNIQUE_KEY = "MVP_UNIQUE_KEY"
    }

    private var stateSaved = false
    private var mvpDelegate = MvpDelegate(this, false)

    constructor() : super()

    @ContentView
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

    override fun startActivity(intent: Intent, options: Bundle?) {
        MvpAppCompatActivity.updateLastStartIntent(intent)
        super.startActivity(intent, options)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        MvpAppCompatActivity.updateLastStartIntent(intent)
        super.startActivityForResult(intent, requestCode)
    }

    override fun startActivity(intent: Intent?) {
        super.startActivity(intent)
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

    /**
     * @return The [MvpDelegate] being used by this Fragment.
     */
    override fun getMvpDelegate(): MvpDelegate<*> = mvpDelegate
}