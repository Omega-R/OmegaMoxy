package com.omegar.mvp.compiler.ksp

import com.google.devtools.ksp.symbol.KSFile
import com.omegar.mvp.RegisterMoxyReflectorPackages
import com.omegar.mvp.compiler.entities.Tagged
import com.omegar.mvp.compiler.processors.OriginatingMarker
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.ksp.addOriginatingKSFile


/**
 * Created by Anton Knyazev on 28.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
internal object KspOriginatingMarker : OriginatingMarker {

    override fun TypeAliasSpec.Builder.addOriginating(tagged: Tagged) {
        tagged.getTag(Files::class)?.let {
            it.files.forEach {
                addOriginatingKSFile(it)
            }
        }
    }

    override fun PropertySpec.Builder.addOriginating(tagged: Tagged) {
        tagged.getTag(Files::class)?.let {
            it.files.forEach {
                addOriginatingKSFile(it)
            }
        }
    }

    override fun FunSpec.Builder.addOriginating(tagged: Tagged) {
        tagged.getTag(Files::class)?.let {
            it.files.forEach {
                addOriginatingKSFile(it)
            }
        }
    }

    override fun TypeSpec.Builder.addOriginating(tagged: Tagged) {
        tagged.getTag(Files::class)?.let {
            it.files.forEach {
                addOriginatingKSFile(it)
            }
        }
    }

    internal data class Files(val files: List<KSFile>)

}