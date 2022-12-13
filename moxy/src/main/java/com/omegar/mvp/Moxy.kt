package com.omegar.mvp;

import static java.lang.annotation.ElementType.TYPE;

import java.lang.annotation.Target;

/**
 * Created by Anton Knyazev on 25.11.2020.
 */
@Target(value = TYPE)
public @interface Moxy {

    String reflectorPackage();

}
