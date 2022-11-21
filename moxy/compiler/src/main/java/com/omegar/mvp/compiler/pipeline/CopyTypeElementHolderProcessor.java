package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.entity.TypeElementHolder;

import javax.lang.model.element.TypeElement;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */

public class CopyTypeElementHolderProcessor<I extends TypeElementHolder> extends Processor<I, I> {

    private final Publisher<TypeElement> mPublisher;

    public CopyTypeElementHolderProcessor(Publisher<TypeElement> publisher) {
        mPublisher = publisher;
    }

    @Override
    protected I process(I input) {
        mPublisher.next(input.getTypeElement());
        return super.process(input);
    }

    @Override
    protected void finish(PipelineContext<I> nextContext) {
        mPublisher.finish();
        super.finish(nextContext);
    }

}
