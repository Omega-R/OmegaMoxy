package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public abstract class Processor<I,O> {

    public void process(I input, PipelineContext<O> context) {
        context.next(process(input));
    }

    protected O process(I input) {
        //noinspection unchecked
        return (O) input;
    }

    protected void finish() {
        // nothing
    }

}
