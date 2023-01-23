package com.omegar.mvp.compiler.pipeline

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
class UniqueValidator<T> : Validator<T>() {

    private val list: MutableList<T> = ArrayList()

    override fun validate(input: T): Boolean {
        if (list.contains(input)) {
            return false
        }
        list.add(input)
        return true
    }

    override fun finish(nextContext: PipelineContext<T>?) {
        super.finish(nextContext)
        list.clear()
    }
}