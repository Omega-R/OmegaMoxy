package com.omegar.mvp.compiler.processors


/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
fun interface Processor<T, R> : (T) -> R {

    override fun invoke(item: T): R

}
