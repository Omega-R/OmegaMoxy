package com.omegar.mvp.compiler.pipeline

import com.squareup.kotlinpoet.FileSpec

/**
 * Date: 27-Jul-17
 * Time: 10:26
 *
 * @author Evgeny Kursakov
 */
abstract class KotlinFileProcessor<M> : Processor<M, KotlinFile>() {
    abstract override fun process(input: M): KotlinFile
}

typealias KotlinFile = FileSpec