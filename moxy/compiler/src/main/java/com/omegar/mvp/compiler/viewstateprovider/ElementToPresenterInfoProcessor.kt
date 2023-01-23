package com.omegar.mvp.compiler.viewstateprovider

import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.PresenterInfo
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo.Companion.getViewFullName
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo.Companion.getViewStateFullName
import com.omegar.mvp.compiler.pipeline.ElementProcessor
import com.omegar.mvp.compiler.pipeline.PipelineContext
import com.omegar.mvp.compiler.pipeline.Publisher
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.util.Elements

class ElementToPresenterInfoProcessor(
    private val mElements: Elements,
    private val mUsedViewsPublisher: Publisher<TypeElement>
) : ElementProcessor<TypeElement, PresenterInfo>() {

    companion object {
        private val MVP_PRESENTER_CLASS = MvpPresenter::class.java.canonicalName
    }

    override fun process(input: TypeElement): PresenterInfo {
        val viewClassName = getViewClassName(input)
        val viewFullName = getViewFullName(mElements, viewClassName)
        return PresenterInfo(
            input,
            getViewStateFullName(viewFullName),
            viewClassName
        )
    }

    private fun getViewClassName(typeElement: TypeElement): TypeElement {
        val view = getViewClassFromGeneric(typeElement)

        // Remove generic from view class name
        val viewWithoutGeneric = Util.substringBefore(view, '<')
        val viewTypeElement = mElements.getTypeElement(viewWithoutGeneric)
            ?: throw IllegalArgumentException("View \"$view\" for $typeElement cannot be found. \n $view")
        mUsedViewsPublisher.next(viewTypeElement)
        return viewTypeElement
    }

    private fun getViewClassFromGeneric(typeElement: TypeElement): String {
        var superclass = typeElement.asType()
        var parentTypes: Map<String, String> = emptyMap()
        while (superclass.kind != TypeKind.NONE) {
            val superclassElement = (superclass as DeclaredType).asElement() as TypeElement
            val typeArguments = superclass.typeArguments
            val typeParameters = superclassElement.typeParameters
            require(typeArguments.size <= typeParameters.size) { "Code generation for interface " + typeElement.simpleName + " failed. Simplify your generics. (" + typeArguments + " vs " + typeParameters + ")" }
            val types: MutableMap<String, String> = HashMap()
            for (i in typeArguments.indices) {
                types[typeParameters[i].toString()] = Util.fillGenerics(parentTypes, typeArguments[i])
            }
            if (superclassElement.toString() == MVP_PRESENTER_CLASS) {
                // MvpPresenter is typed only on View class
                return Util.fillGenerics(parentTypes, typeArguments)
            }
            parentTypes = types
            superclass = superclassElement.superclass
        }
        return ""
    }

    override fun finish(nextContext: PipelineContext<PresenterInfo>?) {
        mUsedViewsPublisher.finish()
        super.finish(nextContext)
    }

}