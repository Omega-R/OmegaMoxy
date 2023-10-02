package com.omegar.mvp

/**
 * Inject view state to [MvpPresenter.mViews] and
 * [MvpPresenter.mViewState] presenter fields. Presenter, annotated with
 * this, should be strongly typed on view interface(not write some like extends
 * MvpPresenter&lt;V extends SuperView&gt;). Otherwise code generation make
 * code, that broke your app.
 */
@Target(AnnotationTarget.CLASS)
annotation class InjectViewState