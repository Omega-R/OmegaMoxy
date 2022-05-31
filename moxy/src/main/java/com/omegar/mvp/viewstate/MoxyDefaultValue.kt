package com.omegar.mvp.viewstate

@Target(AnnotationTarget.PROPERTY_GETTER)
@Retention(value = AnnotationRetention.SOURCE)
annotation class MoxyDefaultValue(
        val value: String
)
