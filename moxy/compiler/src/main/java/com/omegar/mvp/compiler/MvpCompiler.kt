package com.omegar.mvp.compiler

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.entities.View
import com.omegar.mvp.compiler.ksp.KspViewParser
import com.omegar.mvp.compiler.processors.ViewStateGenerator
import com.squareup.kotlinpoet.ksp.writeTo

/**
 * Created by Anton Knyazev on 26.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
class MvpCompiler(environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val codeGenerator = environment.codeGenerator
    private val logger = environment.logger
    private var isProcessed = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (isProcessed) {
            return emptyList()
        }
        isProcessed = true
        val mvpView = resolver.getClassDeclarationByName(MvpView::class.qualifiedName!!)!!.asStarProjectedType()
        val mvpPresenter = resolver.getClassDeclarationByName(MvpPresenter::class.qualifiedName!!)!!.asStarProjectedType()

        val parser = KspViewParser(logger, resolver, mvpView)
        val viewStateGenerator = ViewStateGenerator()

        resolver.getAllFiles()
            .flatMap { it.declarations }
            .filterIsInstance<KSClassDeclaration>()
            .filter { mvpPresenter.isAssignableFrom(it.asStarProjectedType()) }
            .flatMap { generateSequence(seedFunction = { parser(it) }, nextFunction = View::parent) }
            .filter { it.needGenerate }
            .distinctBy { it.className.canonicalName }
            .toList()
            .forEach { viewStateGenerator(it).writeTo(codeGenerator = codeGenerator, aggregating = false) }

        return emptyList()
    }

}