package com.omegar.mvp;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

/**
 * Inject view state to {@link MvpPresenter#mViews} and
 * {@link MvpPresenter#mViewState} presenter fields. Presenter, annotated with
 * this, should be strongly typed on view interface(not write some like extends
 * MvpPresenter&lt;V extends SuperView&gt;). Otherwise code generation make
 * code, that broke your app.
 */
@Target(value = TYPE)
@Inherited
@interface InjectViewState {
}
