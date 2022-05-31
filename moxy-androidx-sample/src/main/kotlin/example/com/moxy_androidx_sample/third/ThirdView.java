package example.com.moxy_androidx_sample.third;

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

import example.com.moxy_androidx_sample.BaseView;

import static com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END_SINGLE;

public interface ThirdView extends BaseView {

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    void thirdMethod();

}
