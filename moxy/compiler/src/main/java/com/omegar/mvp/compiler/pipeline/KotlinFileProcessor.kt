package com.omegar.mvp.compiler.pipeline

import com.squareup.kotlinpoet.FileSpec

typealias KotlinFile = FileSpec

abstract class KotlinFileProcessor<M> : Processor<M, KotlinFile>() {
    abstract override fun process(input: M): KotlinFile
}

