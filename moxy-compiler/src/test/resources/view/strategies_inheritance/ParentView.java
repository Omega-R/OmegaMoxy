package view.strategies_inheritance;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

import view.strategies_inheritance.strategies.ParentDefaultStrategy;
import view.strategies_inheritance.strategies.Strategy1;

public interface ParentView extends MvpView {
	@MoxyViewCommand(custom = ParentDefaultStrategy.class)
	void parentMethod1(); // ParentDefaultStrategy

	@MoxyViewCommand(custom = ParentDefaultStrategy.class)
	void parentMethod2(); // ParentDefaultStrategy

	@MoxyViewCommand(custom = ParentDefaultStrategy.class)
	void parentMethod3(); // ParentDefaultStrategy

	@MoxyViewCommand(custom = Strategy1.class)
	void parentMethodWithStrategy(); // Strategy1
}