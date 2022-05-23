package com.omegar.mvp.viewstate.strategy

import com.omegar.mvp.viewstate.SerializeType
import kotlin.reflect.KClass

/**
 * Date: 16-Dec-15
 * Time: 17:07
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_SETTER)
@Retention(value = AnnotationRetention.RUNTIME)
annotation class StateStrategyType(
        val value: StrategyType,
        val custom: KClass<out StateStrategy> = SkipStrategy::class,
        val tag: String = "",
        val singleInstance: Boolean = false,
        val serializeType: SerializeType = SerializeType.NONE)