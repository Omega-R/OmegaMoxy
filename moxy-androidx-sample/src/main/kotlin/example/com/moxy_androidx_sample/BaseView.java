package example.com.moxy_androidx_sample;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;


import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.ADD_TO_END_SINGLE;

public interface BaseView extends MvpView {

    @StateStrategyType(ADD_TO_END_SINGLE)
    void testFunction();

}