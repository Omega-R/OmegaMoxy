package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class GenericView$$State<T> extends MvpViewState<GenericView<T>> implements GenericView<T> {

	@Override
	public void testEvent(T param) {
		TestEventCommand testEventCommand = new TestEventCommand(param);
		commands.beforeApply(testEventCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (GenericView<T> view : views) {
			view.testEvent(param);
		}

		commands.afterApply(testEventCommand);
	}


	public class TestEventCommand extends ViewCommand<GenericView<T>> {
		public final T param;

		TestEventCommand(T param) {
			super("testEvent", AddToEndStrategy.class);
			this.param = param;
		}

		@Override
		public void apply(GenericView<T> mvpView) {
			mvpView.testEvent(param);
		}
	}
}