package com.omegar.mvp

/**
 * Register MoxyReflector packages from other modules
 */
@Target(AnnotationTarget.CLASS)
annotation class RegisterMoxyReflectorPackages(vararg val value: String)