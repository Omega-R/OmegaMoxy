package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.OneExecutionStateStrategy;
import com.omegar.mvp.viewstate.strategy.SingleStateStrategy;

public class StrategiesView$$State extends MvpViewState<StrategiesView> implements StrategiesView {

	@Override
	public void singleState() {
		SingleStateCommand singleStateCommand = new SingleStateCommand();
		commands.beforeApply(singleStateCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (StrategiesView view : views) {
			view.singleState();
		}

		commands.afterApply(singleStateCommand);
	}

	@Override
	public void oneExecution() {
		OneExecutionCommand oneExecutionCommand = new OneExecutionCommand();
		commands.beforeApply(oneExecutionCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (StrategiesView view : views) {
			view.oneExecution();
		}

		commands.afterApply(oneExecutionCommand);
	}

	@Override
	public void withoutStrategy() {
		WithoutStrategyCommand withoutStrategyCommand = new WithoutStrategyCommand();
		commands.beforeApply(withoutStrategyCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (StrategiesView view : views) {
			view.withoutStrategy();
		}

		commands.afterApply(withoutStrategyCommand);
	}


	public class SingleStateCommand extends ViewCommand<StrategiesView> {
		SingleStateCommand() {
			super("singleState", SingleStateStrategy.class);
		}

		@Override
		public void apply(StrategiesView mvpView) {
			mvpView.singleState();
		}
	}

	public class OneExecutionCommand extends ViewCommand<StrategiesView> {
		OneExecutionCommand() {
			super("oneExecution", OneExecutionStateStrategy.class);
		}

		@Override
		public void apply(StrategiesView mvpView) {
			mvpView.oneExecution();
		}
	}

	public class WithoutStrategyCommand extends ViewCommand<StrategiesView> {
		WithoutStrategyCommand() {
			super("withoutStrategy", AddToEndSingleStrategy.class);
		}

		@Override
		public void apply(StrategiesView mvpView) {
			mvpView.withoutStrategy();
		}
	}
}