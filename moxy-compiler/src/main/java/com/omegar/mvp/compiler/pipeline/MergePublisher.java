package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
public class MergePublisher<O> extends Publisher<O> {
    private final Publisher<O>[] mPublishers;

    @SafeVarargs
    public MergePublisher(Publisher<O>... publishers) {
        mPublishers = publishers;
        LocalContext localContext = new LocalContext();

        for (Publisher<O> publisher : publishers) {
            publisher.publish(localContext);
        }
    }

    @Override
    protected void finish(PipelineContext<O> nextContext) {
        super.finish(nextContext);
        for (Publisher<O> publisher : mPublishers) {
            publisher.finish(nextContext);
        }
    }

    public class LocalContext implements PipelineContext<O> {

        @Override
        public void next(O nextData) {
            MergePublisher.this.next(nextData);
        }

        @Override
        public void finish() {
            // nothing
        }
    }

}
