package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;

public class SimpleView$$State extends MvpViewState<SimpleView> implements SimpleView {

	@Override
	public void testEvent() {
		TestEventCommand testEventCommand = new TestEventCommand();
		commands.beforeApply(testEventCommand);

		if (views == null || views.isEmpty()) {
			return;
		}

		for (SimpleView view : views) {
			view.testEvent();
		}

		commands.afterApply(testEventCommand);
	}


	public class TestEventCommand extends ViewCommand<SimpleView> {
		TestEventCommand() {
			super("testEvent", AddToEndStrategy.class);
		}

		@Override
		public void apply(SimpleView mvpView) {
			mvpView.testEvent();
		}
	}
}