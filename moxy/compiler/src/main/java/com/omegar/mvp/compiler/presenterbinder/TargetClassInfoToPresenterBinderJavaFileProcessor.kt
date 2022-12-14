package com.omegar.mvp.compiler.presenterbinder

import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpProcessor
import com.omegar.mvp.PresenterBinder
import com.omegar.mvp.compiler.Util
import com.omegar.mvp.compiler.entity.TargetClassInfo
import com.omegar.mvp.compiler.entity.TargetPresenterField
import com.omegar.mvp.compiler.pipeline.KotlinFile
import com.omegar.mvp.compiler.pipeline.KotlinFileProcessor
import com.omegar.mvp.presenter.PresenterField
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType

/**
 * 18.12.2015
 *
 *
 * Generates PresenterBinder for class annotated with &#64;InjectPresenters
 *
 *
 * for Sample class with single injected presenter
 * <pre>
 * `&#64;InjectPresenters
 * public class Sample extends MvpActivity implements MyView
 * {
 *
 * &#64;InjectPresenter(type = PresenterType.LOCAL, tag = "SOME_TAG")
 * com.arellomobile.example.MyPresenter mMyPresenter;
 *
 * }
 *
` *
</pre> *
 *
 *
 * PresenterBinderClassGenerator generates PresenterBinder
 *
 *
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
class TargetClassInfoToPresenterBinderJavaFileProcessor : KotlinFileProcessor<TargetClassInfo>() {

    override fun process(targetClassInfo: TargetClassInfo): KotlinFile {
        val targetClassName = targetClassInfo.name
        val element: TypeElement = targetClassInfo.typeElement
        val fields = targetClassInfo.fields

        val className = targetClassName.simpleNames.joinToString("$") + MvpProcessor.PRESENTER_BINDER_SUFFIX
        val classBuilder = TypeSpec.classBuilder(className)
            .addOriginatingElement(targetClassInfo.typeElement)
            .superclass(PresenterBinder::class.asClassName().parameterizedBy(targetClassName))
            .addProperty(generateGetPresentersMethod(fields, targetClassName))
            .addTypes(fields.map { field ->
                generatePresenterBinderClass(element, field, targetClassName)
            })


        return FileSpec.builder(targetClassName.packageName, className)
            .addType(classBuilder.build())
            .indent("\t")
            .build()
    }

    private fun generateGetPresentersMethod(
        fields: List<TargetPresenterField>,
        containerClassName: ClassName
    ): PropertySpec {
        return PropertySpec.builder(
            "presenterFields",
            LIST.parameterizedBy(PresenterField::class.asClassName().parameterizedBy(containerClassName)),
            KModifier.OVERRIDE
        )
            .initializer(
                format = "listOf(" + fields.joinToString { "%L()" } + ")",
                args = fields.map { ClassName.bestGuess(it.generatedClassName) }.toTypedArray()
            )
            .build()
    }

    private fun generatePresenterBinderClass(
        element: TypeElement, field: TargetPresenterField,
        targetClassName: ClassName
    ): TypeSpec {
        val classBuilder = TypeSpec.classBuilder(field.generatedClassName)
            .addOriginatingElement(element)
            .addModifiers(KModifier.PRIVATE)
            .superclass(PresenterField::class.asClassName().parameterizedBy(targetClassName))
            .addSuperclassConstructorParameter("%S", field.tag ?: field.name)
            .addSuperclassConstructorParameter("%T.%L", field.presenterType.declaringClass, field.presenterType.name)
            .apply {
                if (field.presenterId != null) {
                    addSuperclassConstructorParameter("%S", field.presenterId)
                } else {
                    addSuperclassConstructorParameter("null")
                }
            }
            .addFunction(generateBindMethod(field, targetClassName))
            .addFunction(generateProvidePresenterMethod(field, targetClassName))
        val tagProviderMethodName = field.presenterTagProviderMethodName
        if (tagProviderMethodName != null) {
            classBuilder.addFunction(generateGetTagMethod(tagProviderMethodName, targetClassName))
        }
        return classBuilder.build()
    }

    private fun generateBindMethod(
        field: TargetPresenterField,
        targetClassName: ClassName
    ): FunSpec {
        return FunSpec.builder("bind")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("target", targetClassName)
            .addParameter("presenter", MvpPresenter::class.asClassName().parameterizedBy(STAR))
            .addStatement("target.%L = presenter as %T", field.name, field.typeName)
            .build()
    }

    private fun generateProvidePresenterMethod(
        field: TargetPresenterField,
        targetClassName: ClassName
    ): FunSpec {
        val builder = FunSpec.builder("providePresenter")
            .addModifiers(KModifier.OVERRIDE)
            .returns(MvpPresenter::class.asClassName().parameterizedBy(STAR))
            .addParameter("delegated", targetClassName)
        if (field.presenterProviderMethodName != null) {
            builder.addStatement("return delegated.%L()", field.presenterProviderMethodName!!)
        } else {
            val hasEmptyConstructor = Util.hasEmptyConstructor((field.clazz as DeclaredType).asElement() as TypeElement)
            if (hasEmptyConstructor) {
                builder.addStatement("return %T()", field.typeName)
            } else {
                builder.addStatement(
                    "throw %T(%S + %S)",
                    IllegalStateException::class.java,
                    field.clazz,
                    " has not default constructor. You can apply @ProvidePresenter to some method which will construct Presenter. Also you can make it default constructor"
                )
            }
        }
        return builder.build()
    }

    private fun generateGetTagMethod(
        tagProviderMethodName: String,
        targetClassName: ClassName
    ): FunSpec {
        return FunSpec.builder("getTag")
            .addModifiers(KModifier.OVERRIDE)
            .returns(STRING)
            .addParameter("delegated", targetClassName)
            .addStatement("return delegated.%L.toString()", tagProviderMethodName)
            .build()
    }

}