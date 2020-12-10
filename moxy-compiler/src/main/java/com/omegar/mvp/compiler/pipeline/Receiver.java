package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
public abstract class Receiver<I> extends Processor<I, Void> {

    @Override
    public void process(I input, PipelineContext<Void> context) {
        receive(input);
    }

    public abstract void receive(I input);

}
