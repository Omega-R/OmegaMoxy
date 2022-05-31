package view.strategies_inheritance;

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

import view.strategies_inheritance.strategies.ChildDefaultStrategy;
import view.strategies_inheritance.strategies.Strategy2;

public interface ChildView extends ParentView {
	@MoxyViewCommand(custom = ChildDefaultStrategy.class)
	void parentMethod1(); // ParentDefaultStrategy -> ChildDefaultStrategy

	@MoxyViewCommand(custom = Strategy2.class)
	void parentMethod2(); // ParentDefaultStrategy -> Strategy2

	@MoxyViewCommand(custom = ChildDefaultStrategy.class)
	void childMethod(); // ChildDefaultStrategy

	@MoxyViewCommand(custom = Strategy2.class)
	void childMethodWithStrategy(); // Strategy2
}