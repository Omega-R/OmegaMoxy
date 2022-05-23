package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
public class TriplePublisher<FI, SE, TH> extends Publisher<Triple<FI, SE, TH>> {
    private final List<FI> mFirsts = new ArrayList<>();
    private final List<SE> mSeconds = new ArrayList<>();
    private final List<TH> mThirds = new ArrayList<>();
    private final Publisher<FI> mPublisher1;
    private final Publisher<SE> mPublisher2;
    private final Publisher<TH> mPublisher3;


    public TriplePublisher(Publisher<FI> publisher1,
                           Publisher<SE> publisher2,
                           Publisher<TH> publisher3) {
        mPublisher1 = publisher1;
        mPublisher2 = publisher2;
        mPublisher3 = publisher3;
    }

    @Override
    public void publish(PipelineContext<Triple<FI, SE, TH>> context) {
        super.publish(context);

        mPublisher1.publish(new LocalContext<>(mFirsts));
        mPublisher2.publish(new LocalContext<>(mSeconds));
        mPublisher3.publish(new LocalContext<>(mThirds));
    }

    private void maybePublisherNext() {

        if (!mFirsts.isEmpty() && !mSeconds.isEmpty() && !mThirds.isEmpty() ) {
            Iterator<FI> firstIterator = mFirsts.iterator();
            Iterator<SE> secondIterator = mSeconds.iterator();
            Iterator<TH> thirdIterator = mThirds.iterator();

            while (firstIterator.hasNext() && secondIterator.hasNext() && thirdIterator.hasNext() ) {
                FI first = firstIterator.next();
                SE second = secondIterator.next();
                TH third = thirdIterator.next();

                next(new Triple<>(first, second, third));
                firstIterator.remove();
                secondIterator.remove();
                thirdIterator.remove();
            }
        }
    }

    public static <FI, SE, TH> TriplePublisher<Set<FI>, Set<SE>, Set<TH>> collectQuad(Publisher<FI> publisher1,
                                                                                                   Publisher<SE> publisher2,
                                                                                                   Publisher<TH> publisher3) {
        return new TriplePublisher<>(
                publisher1.collect(),
                publisher2.collect(),
                publisher3.collect()
        );
    }

    private class LocalContext<T> implements PipelineContext<T> {

        private final List<T> list;

        public LocalContext(List<T> list) {
            this.list = list;
        }

        @Override
        public void next(T nextData) {
            list.add(nextData);
            maybePublisherNext();
        }

        @Override
        public void finish() {
            // nothing
        }
    }


}
