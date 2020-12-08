package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.MvpCompiler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.tools.Diagnostic;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
public class QuadPublisher<FI, SE, TH, FO> extends Publisher<Quad<FI, SE, TH, FO>> {
    private final List<FI> mFirsts = new ArrayList<>();
    private final List<SE> mSeconds = new ArrayList<>();
    private final List<TH> mThirds = new ArrayList<>();
    private final List<FO> mFourths = new ArrayList<>();
    private final Publisher<FI> mPublisher1;
    private final Publisher<SE> mPublisher2;
    private final Publisher<TH> mPublisher3;
    private final Publisher<FO> mPublisher4;


    public QuadPublisher(Publisher<FI> publisher1,
                         Publisher<SE> publisher2,
                         Publisher<TH> publisher3,
                         Publisher<FO> publisher4) {
        mPublisher1 = publisher1;
        mPublisher2 = publisher2;
        mPublisher3 = publisher3;
        mPublisher4 = publisher4;
    }

    @Override
    public void publish(PipelineContext<Quad<FI, SE, TH, FO>> context) {
        super.publish(context);

        mPublisher1.publish(new LocalContext<>(mFirsts));
        mPublisher2.publish(new LocalContext<>(mSeconds));
        mPublisher3.publish(new LocalContext<>(mThirds));
        mPublisher4.publish(new LocalContext<>(mFourths));
    }

    private void maybePublisherNext() {

        if (!mFirsts.isEmpty() && !mSeconds.isEmpty() && !mThirds.isEmpty() && !mFourths.isEmpty()) {
            Iterator<FI> firstIterator = mFirsts.iterator();
            Iterator<SE> secondIterator = mSeconds.iterator();
            Iterator<TH> thirdIterator = mThirds.iterator();
            Iterator<FO> fourthIterator = mFourths.iterator();

            while (firstIterator.hasNext() && secondIterator.hasNext() && thirdIterator.hasNext() && fourthIterator.hasNext()) {
                FI first = firstIterator.next();
                SE second = secondIterator.next();
                TH third = thirdIterator.next();
                FO fourth = fourthIterator.next();

                next(new Quad<>(first, second, third, fourth));
                firstIterator.remove();
                secondIterator.remove();
                thirdIterator.remove();
                fourthIterator.remove();
            }
        }
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
