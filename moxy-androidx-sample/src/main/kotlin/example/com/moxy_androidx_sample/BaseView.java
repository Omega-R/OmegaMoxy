package example.com.moxy_androidx_sample;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import static com.omegar.mvp.viewstate.strategy.StrategyType.*;

public interface BaseView extends MvpView {

    @StateStrategyType(ADD_TO_END_SINGLE)
    void setTest(float test);

    float getTest();

    @StateStrategyType(ADD_TO_END)
    void testFunction();

}