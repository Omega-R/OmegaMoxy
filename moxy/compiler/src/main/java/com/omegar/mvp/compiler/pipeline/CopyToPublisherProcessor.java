package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */

public class CopyToPublisherProcessor<I> extends Processor<I, I> {

    private final Publisher<I> mPublisher;

    public CopyToPublisherProcessor(Publisher<I> publisher) {
        mPublisher = publisher;
    }

    @Override
    protected I process(I input) {
        mPublisher.next(input);
        return super.process(input);
    }

    @Override
    protected void finish(PipelineContext<I> nextContext) {
        mPublisher.finish();
        super.finish(nextContext);
    }

}
