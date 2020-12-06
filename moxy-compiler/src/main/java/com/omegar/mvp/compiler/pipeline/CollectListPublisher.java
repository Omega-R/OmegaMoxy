package com.omegar.mvp.compiler.pipeline;

import com.omegar.mvp.compiler.MvpCompiler;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.tools.Diagnostic;

/**
 * Created by Anton Knyazev on 05.12.2020.
 */

public class CollectListPublisher<I> extends Publisher<List<I>>{

    private final List<I> mResult = new ArrayList<>();
    private final Publisher<I> mPublisher;

    public CollectListPublisher(Publisher<I> publisher) {
        mPublisher = publisher;
    }

    @Override
    public synchronized void publish(PipelineContext<List<I>> context) {
        super.publish(context);
        mPublisher.publish(new LocalContext());
    }

    private class LocalContext implements PipelineContext<I> {

        @Override
        public void next(I nextData) {
            mResult.add(nextData);
            MvpCompiler.getMessager().printMessage(Diagnostic.Kind.WARNING, "Collect NEXT " + nextData);
        }

        @Override
        public void finish() {
            MvpCompiler.getMessager().printMessage(Diagnostic.Kind.WARNING, "Collect Finish");

            CollectListPublisher.this.next(new ArrayList<>(mResult));
            mResult.clear();
            CollectListPublisher.this.finish();
        }
    }

}
