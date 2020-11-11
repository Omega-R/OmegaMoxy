package view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import static com.omegar.mvp.viewstate.strategy.StrategyType.ONE_EXECUTION;
import static com.omegar.mvp.viewstate.strategy.StrategyType.SINGLE;

@StateStrategyType(ADD_TO_END_SINGLE)
public interface StrategiesView extends MvpView {
	@StateStrategyType(SINGLE)
	void singleState();

	@StateStrategyType(ONE_EXECUTION)
	void oneExecution();

	void withoutStrategy();
}