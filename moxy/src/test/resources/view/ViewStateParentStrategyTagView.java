package view;

import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

/**
 * Date: 26.02.2016
 * Time: 12:13
 *
 * @author Savin Mikhail
 */
public interface ViewStateParentStrategyTagView {
	@StateStrategyType(ADD_TO_END_SINGLE)
	void method1();

	@StateStrategyType(value = AddToEndSingleStrategy.class, tag = "Test2")
	void method2();
}
