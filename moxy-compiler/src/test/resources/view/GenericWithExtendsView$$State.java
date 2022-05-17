package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

import java.io.Serializable;

public class GenericWithExtendsView$$State<T extends Serializable> extends MvpViewState<GenericWithExtendsView<T>> implements GenericWithExtendsView<T> {

	@Override
	public void testEvent(T param) {
		TestEventCommand testEventCommand = new TestEventCommand(param);
		viewCommands.beforeApply(testEventCommand);

		if (mutableViews == null || mutableViews.isEmpty()) {
			return;
		}

		for (GenericWithExtendsView<T> view : mutableViews) {
			view.testEvent(param);
		}

		viewCommands.afterApply(testEventCommand);
	}


	public class TestEventCommand extends ViewCommand<GenericWithExtendsView<T>> {
		public final T param;

		TestEventCommand(T param) {
			super("testEvent", AddToEndStrategy.class);
			this.param = param;
		}

		@Override
		public void apply(GenericWithExtendsView<T> mvpView) {
			mvpView.testEvent(param);
		}
	}
}