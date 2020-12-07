package com.omegar.mvp.compiler.viewstateprovider;

import com.omegar.mvp.compiler.entity.PresenterInfo;
import com.omegar.mvp.compiler.pipeline.Validator;

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
public class NormalPresenterValidator extends Validator<com.omegar.mvp.compiler.entity.PresenterInfo> {

    @Override
    public boolean validate(PresenterInfo input) {
        return !input.isParametrized() && !input.isAbstracted();
    }
}
