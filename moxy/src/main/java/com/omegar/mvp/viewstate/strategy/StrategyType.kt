package com.omegar.mvp.viewstate.strategy

import kotlin.reflect.KClass

/**
 * Created by Anton Knyazev on 09.11.2020.
 */
enum class StrategyType(val strategyClass: KClass<out StateStrategy>?) {
    // Command will be added to end of commands queue.
    ADD_TO_END(AddToEndStrategy::class),
    // Command will be added to end of commands queue. If commands queue contains same type command, then existing command will be removed.
    ADD_TO_END_SINGLE(AddToEndSingleStrategy::class),
    // Command will be added to the end of the commands queue. If the commands queue contains the same tag, then an existing command will be removed.
    ADD_TO_END_SINGLE_TAG(AddToEndSingleTagStrategy::class),
    // Command will be saved in commands queue. And this command will be removed after first execution.
    ONE_EXECUTION(OneExecutionStateStrategy::class),
    // This strategy will clear current commands queue and then incoming command will be put in.
    SINGLE(SingleStateStrategy::class),
    // Command will not be put in commands queue
    SKIP(SkipStrategy::class),
    // Custom strategy
    CUSTOM(null);

}