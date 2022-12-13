package com.omegar.mvp

import java.lang.annotation.Inherited

/**
 * Inject view state to [MvpPresenter.mViews] and
 * [MvpPresenter.mViewState] presenter fields. Presenter, annotated with
 * this, should be strongly typed on view interface(not write some like extends
 * MvpPresenter&lt;V extends SuperView&gt;). Otherwise code generation make
 * code, that broke your app.
 */
@Target(AnnotationTarget.CLASS)
@Inherited
internal annotation class InjectViewState 