package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.viewstate.ViewStateJavaFileProcessor;
import com.omegar.mvp.compiler.viewstateprovider.InjectViewStateProcessor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;

/**
 * Created by Anton Knyazev on 03.12.2020.
 */
@SuppressWarnings("rawtypes")
public class Pipeline {
    private static Context sEmptyContext = new Context.Noop();

    private final List<Processor> mProcessors;

    public Pipeline(Processor... processors) {
        mProcessors = Arrays.asList(processors);
    }

    public void start() {
        Context lastContext = sEmptyContext;
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
            processor.finish();
            nextContext.finish();
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

    public static class Builder {

        private final List<Processor> processors = new ArrayList<>();

        public Builder(Publisher publisher) {
            processors.add(publisher);
        }

        public Builder add(Processor processor) {
            processors.add(processor);
            return this;
        }

        @SuppressWarnings("unchecked")
        public Builder addWithCache(Processor processor) {
            processors.add(new CacheWrapperProcessor(processor));
            return this;
        }

        public Builder unique() {
            processors.add(new UniqueValidator());
            return this;
        }


        public Pipeline build(Receiver receiver) {
            processors.add(receiver);
            return new Pipeline(processors.toArray(new Processor[0]));
        }

    }

        public static Pipeline createViewStatePipeline(RoundEnvironment roundEnv,
                                                       TypeElement annotationClass,
                                                       ElementKind kind,
                                                       String currentMoxyReflectorPackage,
                                                       ProcessingEnvironment processingEnv) {
        return new Builder(new ElementSourceProcessor(roundEnv, annotationClass, kind))
                .add(new InjectViewStateProcessor())
                .addWithCache(new ViewStateJavaFileProcessor(currentMoxyReflectorPackage))
                .build(new FileWriterProcessor(processingEnv));
    }

}
