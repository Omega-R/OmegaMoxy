package view;

import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

/**
 * Date: 26.02.2016
 * Time: 12:13
 *
 * @author Savin Mikhail
 */
public interface ViewStateParentStrategyTagView {
	@MoxyViewCommand(ADD_TO_END_SINGLE)
	void method1();

	@MoxyViewCommand(value = AddToEndSingleStrategy.class, tag = "Test2")
	void method2();
}
