package com.omegar.mvp.viewstate;

import com.omegar.mvp.MoxyReflector;
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
	private final StateStrategy mStateStrategy;

	protected ViewCommand(String tag, StateStrategy stateStrategy) {
		mTag = tag;
		mStateStrategy = stateStrategy;
	}

	protected ViewCommand(String tag, Class<? extends StateStrategy> stateStrategyClass) {
		this(tag, (StateStrategy) MoxyReflector.getStrategy(stateStrategyClass));
	}

	public abstract void apply(View view);

	public String getTag() {
		return mTag;
	}

	public StateStrategy getStateStrategy() {
		return mStateStrategy;
	}
}
