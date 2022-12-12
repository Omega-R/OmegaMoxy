package com.omegar.mvp

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.omegar.mvp.MvpAppCompatDialogFragment.Companion

open class MvpBottomSheetDialogFragment : BottomSheetDialogFragment(), MvpDelegateHolder {

    companion object {

        private const val KEY_UNIQUE_KEY = "UNIQUE_KEY"
    }

    private var stateSaved = false
    private var mvpDelegate: MvpDelegate<out MvpBottomSheetDialogFragment> = MvpDelegate(this)

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
        super.startActivity(intent, options)
        MvpAppCompatActivity.updateLastStartIntent(intent)
    }

    override fun startActivityForResult(intent: Intent, requestCode: Int) {
        super.startActivityForResult(intent, requestCode)
        MvpAppCompatActivity.updateLastStartIntent(intent)
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