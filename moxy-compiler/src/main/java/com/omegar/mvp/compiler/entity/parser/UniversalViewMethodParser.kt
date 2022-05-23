package com.omegar.mvp.compiler.entity.parser

import com.omegar.mvp.compiler.entity.ViewMethod
import com.omegar.mvp.compiler.entity.parser.javax.JavaxViewMethodParser
import com.omegar.mvp.compiler.entity.parser.km.KmViewMethodParser
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.lang.model.element.TypeElement
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

class UniversalViewMethodParser(elements: Elements, types: Types) : ViewMethod.Parser() {
    private val kmViewMethodFactory = KmViewMethodParser(elements, types)
    private val javaxViewMethodFactory = JavaxViewMethodParser(types)

    @KotlinPoetMetadataPreview
    override fun extractTypeVariableNames(targetInterfaceElement: TypeElement): List<TypeVariableName> {
        return targetInterfaceElement.callFunc {
            extractTypeVariableNames(targetInterfaceElement)
        }
    }

    @KotlinPoetMetadataPreview
    override fun parse(targetInterfaceElement: TypeElement): List<ViewMethod> {
        return targetInterfaceElement.callFunc {
            parse(targetInterfaceElement)
        }
    }

    private inline fun <T> TypeElement.callFunc(func: ViewMethod.Parser.() -> T): T {
        return if (kmViewMethodFactory.isPossible(this)) {
            kmViewMethodFactory.func()
        } else {
            javaxViewMethodFactory.func()
        }
    }

}