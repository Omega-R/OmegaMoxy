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
@Retention(AnnotationRetention.SOURCE)
annotation class MoxyViewCommand(
        val value: StrategyType,
        val custom: KClass<out StateStrategy> = SkipStrategy::class,
        val tag: String = "",
        val singleInstance: Boolean = false,
        val serializeType: SerializeType = SerializeType.NONE
)

@Deprecated("Use MoxyViewCommand instead.", replaceWith = ReplaceWith("MoxyViewCommand"))
typealias StateStrategyType = MoxyViewCommand