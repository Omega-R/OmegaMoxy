package com.omegar.mvp.view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

/**
 * Date: 29.02.2016
 * Time: 9:09
 *
 * @author Savin Mikhail
 */
public interface ParentView extends MvpView {
	void withoutStrategyMethod();

	@MoxyViewCommand(ADD_TO_END_SINGLE)
	void customStrategyMethod();

	@MoxyViewCommand(ADD_TO_END_SINGLE)
	void parentOverrideMethodWithCustomStrategy();
}
