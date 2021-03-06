package com.omegar.mvp.sample.kotlin

import android.os.Bundle
import android.support.v7.app.AlertDialog
import com.omegar.mvp.MvpAppCompatActivity
import com.omegar.mvp.presenter.InjectPresenter
import com.omegar.mvp.presenter.PresenterType
import com.omegar.mvp.presenter.ProvidePresenter
import com.omegar.mvp.presenter.ProvidePresenterTag
import com.omegar.mvp.sample.kotlin.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : MvpAppCompatActivity(), DialogView {

    @InjectPresenter(type = PresenterType.GLOBAL)
    lateinit var dialogPresenter: DialogPresenter

    var alertDialog: AlertDialog? = null

    @ProvidePresenterTag(presenterClass = DialogPresenter::class, type = PresenterType.GLOBAL)
    fun provideDialogPresenterTag(): String = "Hello"

    @ProvidePresenter(type = PresenterType.GLOBAL)
    fun provideDialogPresenter() = DialogPresenter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rootView.setOnClickListener { dialogPresenter.onShowDialogClick() }
    }

    override fun showDialog() {
        alertDialog = AlertDialog.Builder(this)
            .setTitle("Title")
            .setMessage("Message")
            .setOnDismissListener { dialogPresenter.onHideDialog() }
            .show()
    }

    override fun hideDialog() {
        alertDialog?.setOnDismissListener { }
        alertDialog?.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()

        hideDialog()
    }
}