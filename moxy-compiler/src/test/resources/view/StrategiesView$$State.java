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
		viewCommands.beforeApply(singleStateCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (StrategiesView view : mutableViews) {
			view.singleState();
		}

		viewCommands.afterApply(singleStateCommand);
	}

	@Override
	public void oneExecution() {
		OneExecutionCommand oneExecutionCommand = new OneExecutionCommand();
		viewCommands.beforeApply(oneExecutionCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (StrategiesView view : mutableViews) {
			view.oneExecution();
		}

		viewCommands.afterApply(oneExecutionCommand);
	}

	@Override
	public void withoutStrategy() {
		WithoutStrategyCommand withoutStrategyCommand = new WithoutStrategyCommand();
		viewCommands.beforeApply(withoutStrategyCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (StrategiesView view : mutableViews) {
			view.withoutStrategy();
		}

		viewCommands.afterApply(withoutStrategyCommand);
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