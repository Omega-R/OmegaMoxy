package example.com.moxy_androidx_sample.first;

import android.location.Location;

import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

import example.com.moxy_androidx_sample.BaseView;
import example.com.moxy_androidx_sample.SecondInterface;
import example.com.moxy_androidx_sample.third.ThirdView;

import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.ADD_TO_END;
import static com.omegar.mvp.viewstate.strategy.BasicStrategyType.ADD_TO_END_SINGLE;

public interface FirstView<M> extends BaseView, ThirdView {

    @StateStrategyType(ADD_TO_END_SINGLE)
    void firstMethod(List<M> item);

    @StateStrategyType(ADD_TO_END_SINGLE)
    void firstCopyMethod(List<Location> item);

    @StateStrategyType(ADD_TO_END_SINGLE)
    void firstLog(M m);

}