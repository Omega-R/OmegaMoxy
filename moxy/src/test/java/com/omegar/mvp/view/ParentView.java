package com.omegar.mvp.view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

/**
 * Date: 29.02.2016
 * Time: 9:09
 *
 * @author Savin Mikhail
 */
public interface ParentView extends MvpView {
	void withoutStrategyMethod();

	@StateStrategyType(ADD_TO_END_SINGLE)
	void customStrategyMethod();

	@StateStrategyType(ADD_TO_END_SINGLE)
	void parentOverrideMethodWithCustomStrategy();
}
