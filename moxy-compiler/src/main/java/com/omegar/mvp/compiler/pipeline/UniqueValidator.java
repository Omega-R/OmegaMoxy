package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Anton Knyazev on 04.12.2020.
 */
public class UniqueValidator<T> extends Validator<T> {

    private final List<T> mList = new ArrayList<>();

    @Override
    public synchronized boolean validate(T input) {
        if (mList.contains(input)) {
            return false;
        }
        mList.add(input);
        return true;
    }

    @Override
    protected void finish(PipelineContext<T> nextContext) {
        super.finish(nextContext);
        mList.clear();
    }

}
