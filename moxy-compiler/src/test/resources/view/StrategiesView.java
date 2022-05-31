package view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

import static com.omegar.mvp.viewstate.strategy.StrategyType.ONE_EXECUTION;
import static com.omegar.mvp.viewstate.strategy.StrategyType.SINGLE;

@MoxyViewCommand(ADD_TO_END_SINGLE)
public interface StrategiesView extends MvpView {
	@MoxyViewCommand(SINGLE)
	void singleState();

	@MoxyViewCommand(ONE_EXECUTION)
	void oneExecution();

	void withoutStrategy();
}