package view.strategies_inheritance;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import view.strategies_inheritance.strategies.ParentDefaultStrategy;
import view.strategies_inheritance.strategies.Strategy1;

public interface ParentView extends MvpView {
	@StateStrategyType(custom = ParentDefaultStrategy.class)
	void parentMethod1(); // ParentDefaultStrategy

	@StateStrategyType(custom = ParentDefaultStrategy.class)
	void parentMethod2(); // ParentDefaultStrategy

	@StateStrategyType(custom = ParentDefaultStrategy.class)
	void parentMethod3(); // ParentDefaultStrategy

	@StateStrategyType(custom = Strategy1.class)
	void parentMethodWithStrategy(); // Strategy1
}