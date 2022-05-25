package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class OverloadingView$$State extends MvpViewState<OverloadingView> implements OverloadingView {

	@Override
	public void method(String string) {
		MethodCommand methodCommand = new MethodCommand(string);
		commands.beforeApply(methodCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (OverloadingView view : views) {
			view.method(string);
		}

		commands.afterApply(methodCommand);
	}

	@Override
	public void method(int number) {
		Method1Command method1Command = new Method1Command(number);
		commands.beforeApply(method1Command);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (OverloadingView view : views) {
			view.method(number);
		}

		commands.afterApply(method1Command);
	}

	@Override
	public void method(Object object) {
		Method2Command method2Command = new Method2Command(object);
		commands.beforeApply(method2Command);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (OverloadingView view : views) {
			view.method(object);
		}

		commands.afterApply(method2Command);
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