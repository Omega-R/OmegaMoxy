package example.com.moxy_androidx_sample.first;

import android.location.Location;

import com.omegar.mvp.viewstate.strategy.MoxyViewCommand;

import java.util.List;

import example.com.moxy_androidx_sample.BaseView;
import example.com.moxy_androidx_sample.third.ThirdView;

import static com.omegar.mvp.viewstate.strategy.StrategyType.ADD_TO_END_SINGLE;

public interface FirstView<M> extends BaseView, ThirdView {

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    void firstMethod(List<M> item);

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    void firstCopyMethod(List<Location> item);

    @MoxyViewCommand(ADD_TO_END_SINGLE)
    void firstLog(M m);

}