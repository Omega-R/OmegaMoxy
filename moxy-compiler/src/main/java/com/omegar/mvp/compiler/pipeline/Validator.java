package com.omegar.mvp.compiler.pipeline;


/**
 * Created by Anton Knyazev on 03.12.2020.
 */
abstract public class Validator<T> extends Processor<T, T> {

    @Override
    public void process(T input, PipelineContext<T> context) {
        if (validate(input)) {
            super.process(input, context);
        }
    }

    public abstract boolean validate(T input);

}
