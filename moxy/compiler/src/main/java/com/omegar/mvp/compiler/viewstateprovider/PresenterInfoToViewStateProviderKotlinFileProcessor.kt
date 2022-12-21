package com.omegar.mvp.compiler.viewstateprovider

import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.ViewStateProvider
import com.omegar.mvp.compiler.MoxyConst
import com.omegar.mvp.compiler.entity.PresenterInfo
import com.omegar.mvp.compiler.pipeline.KotlinFile
import com.omegar.mvp.compiler.pipeline.KotlinFileProcessor
import com.omegar.mvp.viewstate.MvpViewState
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Date: 19-Jan-16
 * Time: 19:51
 *
 * @author Alexander Blinov
 */
class PresenterInfoToViewStateProviderKotlinFileProcessor : KotlinFileProcessor<PresenterInfo>() {

    override fun process(presenterInfo: PresenterInfo): KotlinFile {
        val className = presenterInfo.name.simpleName + MoxyConst.VIEW_STATE_PROVIDER_SUFFIX
        val typeSpec = TypeSpec.classBuilder(className)
            .addOriginatingElement(presenterInfo.typeElement)
            .superclass(ViewStateProvider::class)
            .addProperty(generateGetViewStateMethod(presenterInfo.viewStateName, presenterInfo.viewName))
            .build()
        return FileSpec.builder(presenterInfo.name.packageName, className)
            .addType(typeSpec)
            .indent("\t")
            .build()
    }

    private fun generateGetViewStateMethod(viewState: ClassName, viewName: ClassName): PropertySpec {
        val methodBuilder =
            PropertySpec.builder("viewState", viewState.parameterizedBy(viewName), KModifier.OVERRIDE)
                .getter(
                    FunSpec.getterBuilder()
                        .addStatement("return %T()", viewState)
                        .build()
                )
        return methodBuilder.build()
    }
}