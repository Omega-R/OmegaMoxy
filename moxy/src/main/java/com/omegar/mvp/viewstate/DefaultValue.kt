package com.omegar.mvp.viewstate

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(value = AnnotationRetention.RUNTIME)
annotation class DefaultValue(
        val value: String
)
