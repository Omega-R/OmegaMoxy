package example.com.moxy_androidx_sample.second;

import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import example.com.moxy_androidx_sample.BaseView;

import static com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END_SINGLE;

public interface SecondView extends BaseView {

    @StateStrategyType(ADD_TO_END_SINGLE)
    void secondMethod();

}