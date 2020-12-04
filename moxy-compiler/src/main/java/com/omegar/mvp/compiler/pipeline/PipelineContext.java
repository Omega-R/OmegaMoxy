package com.omegar.mvp.compiler.pipeline;

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
public interface PipelineContext<O> {

    void next(O nextData);

    void finish();

}
