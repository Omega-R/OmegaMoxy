package com.omegar.mvp.compiler

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider


/**
 * Created by Anton Knyazev on 06.06.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class MvpCompilerProvider: SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return MvpCompiler(environment)
    }
}