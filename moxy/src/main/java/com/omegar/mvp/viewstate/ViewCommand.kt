package com.omegar.mvp.viewstate;

import com.omegar.mvp.MoxyReflector;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.SkipStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategy;

import java.io.Serializable;

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

	// default constructor for serializable
	protected ViewCommand() {
		this("", AddToEndSingleStrategy.INSTANCE);
	}

	public abstract void apply(View view);

	public String getTag() {
		return mTag;
	}

	public StateStrategy getStateStrategy() {
		return mStateStrategy;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
