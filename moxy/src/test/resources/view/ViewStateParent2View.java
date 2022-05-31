package view;

import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.SingleStateStrategy;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

/**
 * Date: 26.02.2016
 * Time: 12:13
 *
 * @author Savin Mikhail
 */
public interface ViewStateParent2View {
	@MoxyViewCommand(SingleStateStrategy.class)
	void method1();

	@MoxyViewCommand(value = AddToEndSingleStrategy.class, tag = "Test2")
	void method2();
}
