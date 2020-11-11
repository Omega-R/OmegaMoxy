package com.omegar.mvp.viewstate.strategy;

/**
 * Created by Anton Knyazev on 09.11.2020.
 */
public enum StrategyType {
    ADD_TO_END(AddToEndStrategy.class),
    ADD_TO_END_SINGLE(AddToEndSingleStrategy.class),
    ONE_EXECUTION(OneExecutionStateStrategy.class),
    SINGLE(SingleStateStrategy.class),
    SKIP(SkipStrategy.class),
    CUSTOM(null);

    private Class<? extends StateStrategy> mClassStrategy;

    StrategyType(Class<? extends StateStrategy> clz) {
        mClassStrategy = clz;
    }

    public Class<? extends StateStrategy> getStrategyClass() {
        return mClassStrategy;
    }

}
