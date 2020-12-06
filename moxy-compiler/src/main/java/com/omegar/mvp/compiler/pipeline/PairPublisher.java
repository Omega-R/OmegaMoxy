package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javafx.util.Pair;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */
class PairPublisher<K, V> extends Publisher<Pair<K, V>> {
    private final List<K> mKeys = new ArrayList<>();
    private final List<V> mValues = new ArrayList<>();


    public PairPublisher(Publisher<K> publisher1, Publisher<V> publisher2) {
        publisher1.publish(new KeyContext());
        publisher2.publish(new ValueContext());
    }

    private void nextKeyData(K nextData) {
        mKeys.add(nextData);
        maybeNext();
    }

    private void nextValueData(V nextData) {
        mValues.add(nextData);
        maybeNext();
    }

    private void maybeNext() {
        if (!mKeys.isEmpty() && !mValues.isEmpty()) {
            Iterator<K> keyIterator = mKeys.iterator();
            Iterator<V> valueIterator = mValues.iterator();
            while (keyIterator.hasNext() && valueIterator.hasNext()) {
                K key = keyIterator.next();
                V value = valueIterator.next();
                next(new Pair<>(key, value));
                keyIterator.remove();
                valueIterator.remove();
            }
        }
    }

    public class KeyContext implements PipelineContext<K> {

        @Override
        public void next(K nextData) {
            nextKeyData(nextData);
        }

        @Override
        public void finish() {
            // nothing
        }
    }

    public class ValueContext implements PipelineContext<V> {

        @Override
        public void next(V nextData) {
            nextValueData(nextData);
        }

        @Override
        public void finish() {
            // nothing
        }
    }

}
