package view;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

/**
 * Date: 26.02.2016
 * Time: 12:08
 *
 * @author Savin Mikhail
 */

public interface ViewStateParentView extends MvpView {
	@MoxyViewCommand(value = AddToEndSingleStrategy.class)
	void method1();

	@MoxyViewCommand(value = AddToEndSingleStrategy.class, tag = "Test")
	void method2();
}
