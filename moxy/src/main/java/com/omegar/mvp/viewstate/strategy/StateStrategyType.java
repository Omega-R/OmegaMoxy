package com.omegar.mvp.viewstate.strategy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date: 16-Dec-15
 * Time: 17:07
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
@Target(value = {ElementType.METHOD})
@Retention(value = RetentionPolicy.RUNTIME)
public @interface StateStrategyType {

	StrategyType value();

	Class<? extends StateStrategy> custom() default SkipStrategy.class;

	String tag() default "";

}
