package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
public interface PublisherHolder<I> {

    Publisher<I> getPublisher();

}
