package com.omegar.mvp;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * Created by Anton Knyazev on 25.11.2020.
 */
@Target(value = TYPE)
public @interface Moxy {

    String reflectorPackage();

}
