package view;

import com.omegar.mvp.viewstate.MvpViewState;
import com.omegar.mvp.viewstate.ViewCommand;
import com.omegar.mvp.viewstate.strategy.AddToEndStrategy;
import java.io.Serializable;
import java.lang.Override;

public class ExtendsOfGenericView$$State extends MvpViewState<ExtendsOfGenericView> implements ExtendsOfGenericView {
    @Override
    public void testEvent(Serializable param) {
        TestEventCommand testEventCommand = new TestEventCommand(param);
        viewCommands.beforeApply(testEventCommand);

        if (mutableViews == null || mutableViews.isEmpty()) {
            return;
        }

        for (ExtendsOfGenericView view : mutableViews) {
            view.testEvent(param);
        }

        viewCommands.afterApply(testEventCommand);
    }

    public class TestEventCommand extends ViewCommand<ExtendsOfGenericView> {
        public final Serializable param;

        TestEventCommand(Serializable param) {
            super("testEvent", AddToEndStrategy.class);

            this.param = param;
        }

        @Override
        public void apply(ExtendsOfGenericView mvpView) {
            mvpView.testEvent(param);
        }
    }
}