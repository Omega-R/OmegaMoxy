package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class GenericMethodsView$$State extends MvpViewState<GenericMethodsView> implements GenericMethodsView {

	@Override
	public <T> void generic(T param) {
		GenericCommand genericCommand = new GenericCommand(param);
		viewCommands.beforeApply(genericCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (GenericMethodsView view : mutableViews) {
			view.generic(param);
		}

		viewCommands.afterApply(genericCommand);
	}

	@Override
	public <T extends Number> void genericWithExtends(T param) {
		GenericWithExtendsCommand genericWithExtendsCommand = new GenericWithExtendsCommand(param);
		viewCommands.beforeApply(genericWithExtendsCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (GenericMethodsView view : mutableViews) {
			view.genericWithExtends(param);
		}

		viewCommands.afterApply(genericWithExtendsCommand);
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