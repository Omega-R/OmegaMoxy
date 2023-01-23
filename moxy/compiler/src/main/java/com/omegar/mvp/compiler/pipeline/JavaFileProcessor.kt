package com.omegar.mvp.compiler.pipeline

import com.squareup.javapoet.JavaFile

/**
 * Date: 27-Jul-17
 * Time: 10:26
 *
 * @author Evgeny Kursakov
 */
abstract class JavaFileProcessor<M> : Processor<M, JavaFile>() {

    abstract override fun process(input: M): JavaFile

}