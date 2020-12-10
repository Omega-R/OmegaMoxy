package com.omegar.mvp.viewstate;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategy;

/**
 * Date: 16-Dec-15
 * Time: 16:59
 *
 * @author Alexander Blinov
 */
public abstract class ViewCommand<View extends MvpView> {
	private final String mTag;
	private final Class<? extends StateStrategy> mStateStrategyClass;

	protected ViewCommand(String tag, Class<? extends StateStrategy> stateStrategyClass) {
		mTag = tag;
		mStateStrategyClass = stateStrategyClass;
	}

	public abstract void apply(View view);

	public String getTag() {
		return mTag;
	}

	public Class<? extends StateStrategy> getStrategyClass() {
		return mStateStrategyClass;
	}
}
