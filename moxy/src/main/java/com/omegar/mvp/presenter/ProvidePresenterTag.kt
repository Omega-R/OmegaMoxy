package com.omegar.mvp.presenter

import com.omegar.mvp.MvpPresenter
import kotlin.reflect.KClass

/**
 *
 * Called when Moxy generate presenter tag for search Presenter in [PresenterStore].
 *
 * Requirements:
 *
 *  * presenterClass parameter should be equals with presenter field type
 *  * Presenter Types should be same
 *  * Presenter IDs should be equals
 *
 *
 * Note: if this method stay unused after build, then Moxy never use this method and you should check annotation parameters.
 * <br></br>
 * Date: 14.10.2016
 * Time: 00:09
 *
 * @author Yuri Shmakov
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ProvidePresenterTag(
    val presenterClass: KClass<out MvpPresenter<*>>,
    val type: PresenterType = PresenterType.LOCAL,
    val presenterId: String = ""
)