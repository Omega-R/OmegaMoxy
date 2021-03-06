package com.omegar.mvp.view;

import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import static com.omegar.mvp.viewstate.strategy.StrategyType.SKIP;

/**
 * Date: 29.02.2016
 * Time: 9:10
 *
 * @author Savin Mikhail
 */
public interface ChildView extends ParentView, SimpleInterface {
	@StateStrategyType(SKIP)
	@Override
	void parentOverrideMethodWithCustomStrategy();
}
