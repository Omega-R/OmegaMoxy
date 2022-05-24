package com.omegar.mvp;

import android.os.Bundle;

import com.omegar.mvp.presenter.PresenterType;
import com.omegar.mvp.viewstate.MvpViewState;

import java.util.Set;

/**
 * Date: 15.12.2015
 * Time: 19:31
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 * @author Konstantin Tckhovrebov
 */
@InjectViewState
public abstract class MvpPresenter<View extends MvpView> {
	private boolean mFirstLaunch = true;
	private String mTag;
	private PresenterType mPresenterType;
	private MvpViewState<View> mViewState;

	@SuppressWarnings("unchecked")
	public MvpPresenter() {
        mViewState = (MvpViewState<View>) MoxyReflector.getViewState(getClass());
	}

	public void onCreate(Bundle bundle) {
		if (bundle != null && mFirstLaunch) {
			mViewState.loadState(bundle);
		}
	}

	/**
	 * <p>Attach view to view state or to presenter(if view state not exists).</p>
	 * <p>If you use {@link MvpDelegate}, you should not call this method directly.
	 * It will be called on {@link MvpDelegate#onAttach()}, if view does not attached.</p>
	 *
	 * @param view to attachment
	 */
	protected void attachView(View view) {
		mViewState.attachView(view);

		attachView(view, mFirstLaunch);

		if (mFirstLaunch) {
			mFirstLaunch = false;

			onFirstViewAttach();
		}
	}

	/**
	 * <p>Attach view to view state or to presenter(if view state not exists).</p>
	 * <p>If you use {@link MvpDelegate}, you should not call this method directly.
	 * It will be called on {@link MvpDelegate#onAttach()}, if view does not attached.</p>
	 *
	 * @param view to attachment
     * @param isFirstAttach is first presenter init and view binding
	 */
	@SuppressWarnings("unused")
	protected void attachView(View view, boolean isFirstAttach) {
	}

	/**
	 * <p>Callback after first presenter init and view binding. If this
	 * presenter instance will have to attach some view in future, this method
	 * will not be called.</p>
	 * <p>There you can to interact with {@link #mViewState}.</p>
	 */
	protected void onFirstViewAttach() {
	}

	/**
	 * <p>Detach view from view state or from presenter(if view state not exists).</p>
	 * <p>If you use {@link MvpDelegate}, you should not call this method directly.
	 * It will be called on {@link MvpDelegate#onDetach()}.</p>
	 *
	 * @param view view to detach
	 */
	@SuppressWarnings("WeakerAccess")
	protected void detachView(View view) {
		mViewState.detachView(view);
	}

	protected void destroyView(View view) {
		mViewState.destroyView(view);
	}

	/**
	 * @return views attached to view state, or attached to presenter(if view state not exists)
	 */
	@SuppressWarnings("WeakerAccess")
	protected Set<View> getAttachedViews() {
		return mViewState.getViews();
	}

	/**
	 * @return view state, casted to view interface for simplify
	 */
	@SuppressWarnings("unchecked")
	protected View getViewState() {
		return (View) mViewState;
	}

	/**
	 * Set view state to presenter
	 *
	 * @param viewState that implements type, setted as View generic param
	 */
	@SuppressWarnings("unused")
	protected void setViewState(MvpViewState<View> viewState) {
		mViewState = viewState;
	}

	/**
	 * Check if view is in restore state or not
	 *
	 * @param view view for check
	 * @return true if view state restore state to incoming view. false otherwise.
	 */
	@SuppressWarnings("unused")
	protected boolean isInRestoreState(View view) {
		return mViewState.isInRestoreState(view);
	}

	PresenterType getPresenterType() {
		return mPresenterType;
	}

	void setPresenterType(PresenterType presenterType) {
		mPresenterType = presenterType;
	}

	String getTag() {
		return mTag;
	}

	void setTag(String tag) {
		mTag = tag;
	}

	public void onSaveInstanceState(Bundle outState) {
		mViewState.saveState(outState);
	}

	/**
	 * <p>Called before reference on this presenter will be cleared and instance of presenter
	 * will be never used.</p>
	 */
	protected void onDestroy() {
	}

}
