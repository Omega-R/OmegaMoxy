package com.omegar.mvp.sample.github.mvp.views;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.sample.github.mvp.models.Repository;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

/**
 * Date: 27.01.2016
 * Time: 20:00
 *
 * @author Yuri Shmakov
 */
@StateStrategyType(AddToEndSingleStrategy.class)
public interface HomeView extends MvpView {
	void showDetailsContainer();

	void setSelection(int position);

	@StateStrategyType(OneExecutionStateStrategy.class)
	void showDetails(int position, Repository repository);
}
