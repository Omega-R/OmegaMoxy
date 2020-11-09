package view.strategies_inheritance;

import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import view.strategies_inheritance.strategies.ChildDefaultStrategy;
import view.strategies_inheritance.strategies.Strategy2;

public interface ChildView extends ParentView {
	@StateStrategyType(custom = ChildDefaultStrategy.class)
	void parentMethod1(); // ParentDefaultStrategy -> ChildDefaultStrategy

	@StateStrategyType(custom = Strategy2.class)
	void parentMethod2(); // ParentDefaultStrategy -> Strategy2

	@StateStrategyType(custom = ChildDefaultStrategy.class)
	void childMethod(); // ChildDefaultStrategy

	@StateStrategyType(custom = Strategy2.class)
	void childMethodWithStrategy(); // Strategy2
}