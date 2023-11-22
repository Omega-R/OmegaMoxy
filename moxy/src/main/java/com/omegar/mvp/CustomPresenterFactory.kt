package com.omegar.mvp

import com.omegar.mvp.presenter.PresenterField
import com.omegar.mvp.presenter.PresenterType
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

/**
 * Created by Anton Knyazev on 25.11.2020.
 */

class CustomPresenterFactory<P : MvpPresenter<*>, D>(presenterClass: KClass<P>, private val factoryBlock: () -> P) :
    PresenterField<D, P>(PresenterType.LOCAL, presenterClass) {

    private var presenter: P? = null

    override fun bind(container: D, presenter: P) {
        this.presenter = presenter
    }

    override fun providePresenter(delegated: D): P = factoryBlock()

    operator fun getValue(thisRef: D, property: KProperty<*>): P {
        return presenter ?: throw NullPointerException("Presenter ${presenterClass.simpleName} not provided")
    }

}

