package com.omegar.mvp.compiler.pipeline;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */

public class CollectSetPublisher<I> extends Publisher<Set<I>>{

    private final Set<I> mResult = new LinkedHashSet<>();
    private final Publisher<I> mPublisher;

    public CollectSetPublisher(Publisher<I> publisher) {
        mPublisher = publisher;
    }

    @Override
    public void publish(PipelineContext<Set<I>> context) {
        super.publish(context);
        mPublisher.publish(new LocalContext());
    }

    private class LocalContext implements PipelineContext<I> {

        @Override
        public void next(I nextData) {
            mResult.add(nextData);
        }

        @Override
        public void finish() {
            CollectSetPublisher.this.next(new LinkedHashSet<>(mResult));
            mResult.clear();
            CollectSetPublisher.this.finish();
        }
    }

}
