package com.omegar.mvp.presenter

/**
 * Date: 17.12.2015
 * Time: 14:54
 *
 * @author Yuri Shmakov
 * @author Alexander BLinov
 */
@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class InjectPresenter(
    val type: PresenterType = PresenterType.LOCAL,
)