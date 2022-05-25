package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class GenericMethodsView$$State extends MvpViewState<GenericMethodsView> implements GenericMethodsView {

	@Override
	public <T> void generic(T param) {
		GenericCommand genericCommand = new GenericCommand(param);
		commands.beforeApply(genericCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (GenericMethodsView view : views) {
			view.generic(param);
		}

		commands.afterApply(genericCommand);
	}

	@Override
	public <T extends Number> void genericWithExtends(T param) {
		GenericWithExtendsCommand genericWithExtendsCommand = new GenericWithExtendsCommand(param);
		commands.beforeApply(genericWithExtendsCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (GenericMethodsView view : views) {
			view.genericWithExtends(param);
		}

		commands.afterApply(genericWithExtendsCommand);
	}


	public class GenericCommand<T> extends ViewCommand<GenericMethodsView> {
		public final T param;

		GenericCommand(T param) {
			super("generic", AddToEndStrategy.class);
			this.param = param;
		}

		@Override
		public void apply(GenericMethodsView mvpView) {
			mvpView.generic(param);
		}
	}

	public class GenericWithExtendsCommand<T extends Number> extends ViewCommand<GenericMethodsView> {
		public final T param;

		GenericWithExtendsCommand(T param) {
			super("genericWithExtends", AddToEndStrategy.class);
			this.param = param;
		}

		@Override
		public void apply(GenericMethodsView mvpView) {
			mvpView.genericWithExtends(param);
		}
	}
}