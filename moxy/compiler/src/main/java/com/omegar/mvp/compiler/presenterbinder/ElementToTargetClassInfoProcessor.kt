package com.omegar.mvp.compiler.presenterbinder

import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.PresenterProviderMethod
import com.omegar.mvp.compiler.entity.TagProviderMethod
import com.omegar.mvp.compiler.entity.TargetClassInfo
import com.omegar.mvp.compiler.entity.TargetPresenterField
import com.omegar.mvp.compiler.pipeline.ElementProcessor
import com.omegar.mvp.presenter.InjectPresenter
import com.omegar.mvp.presenter.ProvidePresenter
import com.omegar.mvp.presenter.ProvidePresenterTag
import javax.lang.model.element.ElementKind
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

class ElementToTargetClassInfoProcessor : ElementProcessor<TypeElement, TargetClassInfo>() {

    override fun process(input: TypeElement): TargetClassInfo {
        // gather presenter fields info
        val fields = collectFields(input)
        bindProvidersToFields(fields, collectPresenterProviders(input))
        bindTagProvidersToFields(fields, collectTagProviders(input))
        return TargetClassInfo(input, fields)
    }

    companion object {
        private val PRESENTER_FIELD_ANNOTATION = InjectPresenter::class.java.name
        private val PROVIDE_PRESENTER_ANNOTATION = ProvidePresenter::class.java.name
        private val PROVIDE_PRESENTER_TAG_ANNOTATION = ProvidePresenterTag::class.java.name
        private fun collectFields(presentersContainer: TypeElement): List<TargetPresenterField> {
            val fields: MutableList<TargetPresenterField> = ArrayList()
            for (element in presentersContainer.enclosedElements) {
                if (element.kind != ElementKind.FIELD) {
                    continue
                }
                val annotation = Util.getAnnotation(element, PRESENTER_FIELD_ANNOTATION)
                    ?: continue

                // TODO: simplify?
                val clazz = (element.asType() as DeclaredType).asElement().asType()
                val name = element.toString()
                val type = Util.getAnnotationValueAsString(annotation, "type")
                val field = TargetPresenterField(clazz, name, type)
                fields.add(field)
            }
            return fields
        }

        private fun collectPresenterProviders(presentersContainer: TypeElement): List<PresenterProviderMethod> {
            val providers: MutableList<PresenterProviderMethod> = ArrayList()
            for (element in presentersContainer.enclosedElements) {
                if (element.kind != ElementKind.METHOD) {
                    continue
                }
                val providerMethod = element as ExecutableElement
                val annotation = Util.getAnnotation(element, PROVIDE_PRESENTER_ANNOTATION)
                    ?: continue
                val name = providerMethod.simpleName.toString()
                val kind = providerMethod.returnType as DeclaredType
                val type = Util.getAnnotationValueAsString(annotation, "type")
                val tag = Util.getAnnotationValueAsString(annotation, "tag")
                val presenterId = Util.getAnnotationValueAsString(annotation, "presenterId")
                providers.add(PresenterProviderMethod(kind, name, type, tag, presenterId))
            }
            return providers
        }

        private fun collectTagProviders(presentersContainer: TypeElement): List<TagProviderMethod> {
            val providers: MutableList<TagProviderMethod> = ArrayList()
            for (element in presentersContainer.enclosedElements) {
                if (element.kind != ElementKind.METHOD) {
                    continue
                }
                val providerMethod = element as ExecutableElement
                val annotation = Util.getAnnotation(element, PROVIDE_PRESENTER_TAG_ANNOTATION)
                    ?: continue
                val name = providerMethod.simpleName.toString()
                val presenterClass = Util.getAnnotationValueAsTypeMirror(annotation, "presenterClass")
                val type = Util.getAnnotationValueAsString(annotation, "type")
                val presenterId = Util.getAnnotationValueAsString(annotation, "presenterId")
                providers.add(TagProviderMethod(presenterClass, name, type, presenterId))
            }
            return providers
        }

        private fun bindProvidersToFields(
            fields: List<TargetPresenterField>,
            presenterProviders: List<PresenterProviderMethod>
        ) {
            if (fields.isEmpty() || presenterProviders.isEmpty()) {
                return
            }
            for (presenterProvider in presenterProviders) {
                val providerTypeMirror = presenterProvider.clazz.asElement().asType()
                for (field in fields) {
                    if (field.clazz == providerTypeMirror) {
                        if (field.presenterType !== presenterProvider.presenterType) {
                            continue
                        }
                        field.presenterProviderMethodName = presenterProvider.name
                    }
                }
            }
        }

        private fun bindTagProvidersToFields(
            fields: List<TargetPresenterField>,
            tagProviders: List<TagProviderMethod>
        ) {
            if (fields.isEmpty() || tagProviders.isEmpty()) {
                return
            }
            for (tagProvider in tagProviders) {
                for (field in fields) {
                    if (field.clazz == tagProvider.presenterClass) {
                        if (field.presenterType !== tagProvider.type) {
                            continue
                        }
                        field.presenterTagProviderMethodName = tagProvider.methodName
                    }
                }
            }
        }
    }
}