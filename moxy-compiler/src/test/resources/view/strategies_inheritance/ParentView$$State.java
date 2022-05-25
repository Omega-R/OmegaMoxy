package view.strategies_inheritance;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;

import view.strategies_inheritance.strategies.ParentDefaultStrategy;
import view.strategies_inheritance.strategies.Strategy1;

public class ParentView$$State extends MvpViewState<ParentView> implements ParentView {

	@Override
	public void parentMethod1() {
		ParentMethod1Command parentMethod1Command = new ParentMethod1Command();
		commands.beforeApply(parentMethod1Command);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (ParentView view : views) {
			view.parentMethod1();
		}

		commands.afterApply(parentMethod1Command);
	}

	@Override
	public void parentMethod2() {
		ParentMethod2Command parentMethod2Command = new ParentMethod2Command();
		commands.beforeApply(parentMethod2Command);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (ParentView view : views) {
			view.parentMethod2();
		}

		commands.afterApply(parentMethod2Command);
	}

	@Override
	public void parentMethod3() {
		ParentMethod3Command parentMethod3Command = new ParentMethod3Command();
		commands.beforeApply(parentMethod3Command);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (ParentView view : views) {
			view.parentMethod3();
		}

		commands.afterApply(parentMethod3Command);
	}

	@Override
	public void parentMethodWithStrategy() {
		ParentMethodWithStrategyCommand parentMethodWithStrategyCommand = new ParentMethodWithStrategyCommand();
		commands.beforeApply(parentMethodWithStrategyCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (ParentView view : views) {
			view.parentMethodWithStrategy();
		}

		commands.afterApply(parentMethodWithStrategyCommand);
	}


	public class ParentMethod1Command extends ViewCommand<ParentView> {
		ParentMethod1Command() {
			super("parentMethod1", ParentDefaultStrategy.class);
		}

		@Override
		public void apply(ParentView mvpView) {
			mvpView.parentMethod1();
		}
	}

	public class ParentMethod2Command extends ViewCommand<ParentView> {
		ParentMethod2Command() {
			super("parentMethod2", ParentDefaultStrategy.class);
		}

		@Override
		public void apply(ParentView mvpView) {
			mvpView.parentMethod2();
		}
	}

	public class ParentMethod3Command extends ViewCommand<ParentView> {
		ParentMethod3Command() {
			super("parentMethod3", ParentDefaultStrategy.class);
		}

		@Override
		public void apply(ParentView mvpView) {
			mvpView.parentMethod3();
		}
	}

	public class ParentMethodWithStrategyCommand extends ViewCommand<ParentView> {
		ParentMethodWithStrategyCommand() {
			super("parentMethodWithStrategy", Strategy1.class);
		}

		@Override
		public void apply(ParentView mvpView) {
			mvpView.parentMethodWithStrategy();
		}
	}
}