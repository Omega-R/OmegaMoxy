package com.omegar.mvp.viewstate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.omegar.mvp.MoxyReflector;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategy;

/**
 * Date: 17.12.2015
 * Time: 11:09
 *
 * @author Yuri Shmakov
 */
public class ViewCommands<View extends MvpView> {
	private final List<ViewCommand<View>> mState = new ArrayList<>();

	public void beforeApply(ViewCommand<View> viewCommand) {
		StateStrategy stateStrategy = getStateStrategy(viewCommand);

		stateStrategy.beforeApply(mState, viewCommand);
	}

	public void afterApply(ViewCommand<View> viewCommand) {
		StateStrategy stateStrategy = getStateStrategy(viewCommand);

		stateStrategy.afterApply(mState, viewCommand);
	}

	private StateStrategy getStateStrategy(ViewCommand<View> viewCommand) {
		StateStrategy stateStrategy = (StateStrategy) MoxyReflector.getStrategy(viewCommand.getStrategyClass());

		if (stateStrategy == null) {
			stateStrategy = newInstanceStateStrategyByReflection(viewCommand);
		}

		return stateStrategy;
	}

	private StateStrategy newInstanceStateStrategyByReflection(ViewCommand<View> viewCommand) {
		//noinspection TryWithIdenticalCatches
		try {
			return viewCommand.getStrategyClass().newInstance();
		} catch (InstantiationException e) {
			throw new IllegalArgumentException("Unable to create state strategy: " + viewCommand.toString());
		} catch (IllegalAccessException e) {
			throw new IllegalArgumentException("Unable to create state strategy: " + viewCommand.toString());
		}
	}

	public boolean isEmpty() {
		return mState.isEmpty();
	}

	public void reapply(View view, Set<ViewCommand<View>> currentState) {
		final ArrayList<ViewCommand<View>> commands = new ArrayList<>(mState);

		for (ViewCommand<View> command : commands) {
			if (currentState.contains(command)) {
				continue;
			}

			command.apply(view);

			afterApply(command);
		}
	}

	public List<ViewCommand<View>> getCurrentState() {
		return mState;
	}
}
