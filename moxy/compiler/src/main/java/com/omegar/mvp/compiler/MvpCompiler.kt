package com.omegar.mvp.compiler

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.omegar.mvp.InjectViewState
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.ksp.KspViewParser
import com.omegar.mvp.compiler.processors.MoxyReflectorGenerator
import com.omegar.mvp.compiler.processors.ViewStateGenerator
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class MvpCompiler(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private var isProcessed = false
    private val currentReflectorPackageName =
        environment.options.getOrDefault("moxyReflectorPackage", NamingRules.moxyReflectorPackageName)


    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isProcessed) {
            return emptyList()
        }
        isProcessed = true

        val parser = KspViewParser(currentReflectorPackageName, logger, resolver)
        val viewStateGenerator = ViewStateGenerator()
        val moxyReflectorGenerator = MoxyReflectorGenerator(currentReflectorPackageName)

        val symbols = resolver.getSymbolsWithAnnotation(InjectViewState::class.qualifiedName!!)

        val views = symbols
            .filterIsInstance<KSClassDeclaration>()
            .flatMap {
                generateSequence(seedFunction = { parser(it) }, nextFunction = View::parent)
            }
            .distinctBy { it.presenterClassName.canonicalName + it.className.canonicalName }
            .onEach {
                if (it.reflectorPackage == currentReflectorPackageName) {
                    viewStateGenerator(it).writeTo(codeGenerator = codeGenerator, aggregating = true)
                }
            }
            .toList()

        if (currentReflectorPackageName == NamingRules.moxyReflectorPackageName) {
            moxyReflectorGenerator(views).writeTo(codeGenerator = codeGenerator, aggregating = true)
        }

        return emptyList()
    }

}