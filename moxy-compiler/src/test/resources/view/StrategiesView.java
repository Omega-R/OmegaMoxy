package view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.BasicStrategyType;
import com.omegar.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.omegar.mvp.viewstate.strategy.SingleStateStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.ONE_EXECUTION;
import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.SINGLE;

@StateStrategyType(ADD_TO_END_SINGLE)
public interface StrategiesView extends MvpView {
	@StateStrategyType(SINGLE)
	void singleState();

	@StateStrategyType(ONE_EXECUTION)
	void oneExecution();

	void withoutStrategy();
}