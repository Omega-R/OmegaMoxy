package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
interface PipelineContext<O> {
    fun next(nextData: O)
    fun finish()
}