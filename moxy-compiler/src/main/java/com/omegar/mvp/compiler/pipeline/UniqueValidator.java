package com.omegar.mvp.compiler.pipeline;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

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
    protected synchronized void finish() {
        super.finish();
        mList.clear();
    }

}
