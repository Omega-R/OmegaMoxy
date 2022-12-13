package com.omegar.mvp.presenter

import com.omegar.mvp.MvpPresenter

/**
 * Date: 18-Dec-15
 * Time: 17:50
 *
 * @author Alexander Blinov
 * @author Yuri Shmakov
 */
abstract class PresenterField<PresentersContainer> protected constructor(
    protected val tag: String,
    val presenterType: PresenterType,
    val presenterId: String?
) {

    abstract fun bind(container: PresentersContainer, presenter: MvpPresenter<*>?)

    abstract fun providePresenter(delegated: PresentersContainer): MvpPresenter<*>?

    // Delegated may be used from generated code if user plane to generate tag at runtime
    open fun getTag(delegated: PresentersContainer): String {
        return tag
    }

}