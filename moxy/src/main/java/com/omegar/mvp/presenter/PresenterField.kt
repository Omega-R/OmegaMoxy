package com.omegar.mvp.presenter

import com.omegar.mvp.MvpPresenter
import kotlin.reflect.KClass

/**
 * Date: 18-Dec-15
 * Time: 17:50
 *
 * @author Alexander Blinov
 * @author Yuri Shmakov
 */
abstract class PresenterField<PresentersContainer, Presenter : MvpPresenter<*>> protected constructor(
    val presenterType: PresenterType,
    val presenterClass: KClass<Presenter>
) {

    abstract fun bind(container: PresentersContainer, presenter: Presenter)

    abstract fun providePresenter(delegated: PresentersContainer): Presenter

}