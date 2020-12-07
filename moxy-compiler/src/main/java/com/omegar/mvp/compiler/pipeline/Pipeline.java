package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.TypeElement;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
@SuppressWarnings("rawtypes")
public class Pipeline {
    private static final Context sNoopContext = new Context.Noop();

    private final List<Processor> mProcessors;

    public Pipeline(List<Processor> processors) {
        mProcessors = processors;
    }

    public void start() {
        Context lastContext = sNoopContext;
        for (int i = mProcessors.size() - 1; i >= 0; i--) {
            Processor processor = mProcessors.get(i);
            lastContext = new Context(processor, lastContext);
        }
        lastContext.next(null);
    }

    @SuppressWarnings("unchecked")
    private static class Context implements PipelineContext {

        private final Processor processor;
        private final Context nextContext;

        public Context(Processor processor, Context nextContext) {
            this.processor = processor;
            this.nextContext = nextContext;
        }

        @Override
        public void next(Object nextData) {
            processor.process(nextData, nextContext);
        }

        @Override
        public void finish() {
            processor.finish(nextContext);
        }

        private static class Noop extends Context {

            public Noop() {
                super(null, null);
            }

            @Override
            public void next(Object nextData) {
                // nothing
            }

            @Override
            public void finish() {
                // nothing
            }
        }

    }

    @SuppressWarnings("unchecked")
    public static class Builder<I, O> {

        private final List<Processor> processors = new ArrayList<>();

        public Builder(Publisher<I> publisher) {
            processors.add(publisher);
        }

        public <T> Builder<T, ?> addProcessor(Processor<I, T> processor) {
            processors.add(processor);
            return (Builder<T, ?>) this;
        }

        public Builder<I, O> addValidator(Validator<I> validator) {
            return (Builder<I, O>) addProcessor(validator);
        }

        public Builder<I, O> unique() {
            processors.add(new UniqueValidator<I>());
            return this;
        }

        public Builder<I, O> copyPublishTo(Publisher<I> publisher) {
            return addProcessor(new CopyToPublisherProcessor(publisher));
        }

        public Builder<I, O> copyTypeElementTo(Publisher<TypeElement> publisher) {
            return addProcessor(new CopyTypeElementHolderProcessor(publisher));
        }

        public Pipeline buildPipeline(Receiver<I> receiver) {
            processors.add(receiver);
            return new Pipeline(processors);
        }

    }

}
