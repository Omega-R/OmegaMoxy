package com.omegar.mvp.compiler.processors

import com.omegar.mvp.compiler.entities.Tagged
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeAliasSpec
import com.squareup.kotlinpoet.TypeSpec


/**
 * Created by Anton Knyazev on 28.04.2023.
 * Copyright (c) 2023 Omega https://omega-r.com
 */
interface OriginatingMarker {

    fun TypeAliasSpec.Builder.addOriginating(tagged: Tagged)

    fun PropertySpec.Builder.addOriginating(tagged: Tagged)

    fun FunSpec.Builder.addOriginating(tagged: Tagged)

    fun TypeSpec.Builder.addOriginating(tagged: Tagged)

}

fun TypeAliasSpec.Builder.addOriginating(tagged: Tagged) = apply {
    tagged.getTag(OriginatingMarker::class)?.run {
        addOriginating(tagged)
    }
}

fun PropertySpec.Builder.addOriginating(tagged: Tagged) = apply {
    tagged.getTag(OriginatingMarker::class)?.run {
        addOriginating(tagged)
    }
}

fun FunSpec.Builder.addOriginating(tagged: Tagged) = apply {
    tagged.getTag(OriginatingMarker::class)?.run {
        addOriginating(tagged)
    }
}

fun TypeSpec.Builder.addOriginating(tagged: Tagged) = apply {
    tagged.getTag(OriginatingMarker::class)?.run {
        addOriginating(tagged)
    }
}

fun TypeSpec.Builder.addOriginating(tagged: List<Tagged>): TypeSpec.Builder = apply {
    tagged.forEach { addOriginating(it) }
}