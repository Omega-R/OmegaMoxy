package example.com.moxy_androidx_sample.third;

import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import example.com.moxy_androidx_sample.BaseView;

import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.ADD_TO_END_SINGLE;

public interface ThirdView extends BaseView {

    @StateStrategyType(ADD_TO_END_SINGLE)
    void thirdMethod();

}
