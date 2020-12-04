package com.omegar.mvp.viewstate.strategy;

/**
 * Created by Anton Knyazev on 02.12.2020.
 */
interface StrategyProvider {

    <T extends StrategyType> StrategyType getStrategyType(Class<T> strategyClass, String tag);

}
