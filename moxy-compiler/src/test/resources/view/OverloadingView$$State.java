package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class OverloadingView$$State extends MvpViewState<OverloadingView> implements OverloadingView {

	@Override
	public void method(String string) {
		MethodCommand methodCommand = new MethodCommand(string);
		viewCommands.beforeApply(methodCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (OverloadingView view : mutableViews) {
			view.method(string);
		}

		viewCommands.afterApply(methodCommand);
	}

	@Override
	public void method(int number) {
		Method1Command method1Command = new Method1Command(number);
		viewCommands.beforeApply(method1Command);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (OverloadingView view : mutableViews) {
			view.method(number);
		}

		viewCommands.afterApply(method1Command);
	}

	@Override
	public void method(Object object) {
		Method2Command method2Command = new Method2Command(object);
		viewCommands.beforeApply(method2Command);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (OverloadingView view : mutableViews) {
			view.method(object);
		}

		viewCommands.afterApply(method2Command);
	}


	public class MethodCommand extends ViewCommand<OverloadingView> {
		public final String string;

		MethodCommand(String string) {
			super("method", AddToEndStrategy.class);
			this.string = string;
		}

		@Override
		public void apply(OverloadingView mvpView) {
			mvpView.method(string);
		}
	}

	public class Method1Command extends ViewCommand<OverloadingView> {
		public final int number;

		Method1Command(int number) {
			super("method", AddToEndStrategy.class);
			this.number = number;
		}

		@Override
		public void apply(OverloadingView mvpView) {
			mvpView.method(number);
		}
	}

	public class Method2Command extends ViewCommand<OverloadingView> {
		public final Object object;

		Method2Command(Object object) {
			super("method", AddToEndStrategy.class);
			this.object = object;
		}

		@Override
		public void apply(OverloadingView mvpView) {
			mvpView.method(object);
		}
	}
}