package com.omegar.mvp;

import com.omegar.mvp.viewstate.MvpViewState;

import java.lang.annotation.Inherited;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Created by Anton Knyazev on 28.08.2020.
 */
@Target(value = TYPE)
@Inherited
public @interface NotGeneratedParentClasses {


}

