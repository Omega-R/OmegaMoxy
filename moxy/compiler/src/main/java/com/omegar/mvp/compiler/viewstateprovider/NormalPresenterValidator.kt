package com.omegar.mvp.compiler.viewstateprovider

import com.omegar.mvp.compiler.entity.PresenterInfo
import com.omegar.mvp.compiler.pipeline.Validator

/**
 * Created by Anton Knyazev on 07.12.2020.
 */
class NormalPresenterValidator : Validator<PresenterInfo>() {

    override fun validate(input: PresenterInfo): Boolean = !input.isParametrized && !input.isAbstracted

}