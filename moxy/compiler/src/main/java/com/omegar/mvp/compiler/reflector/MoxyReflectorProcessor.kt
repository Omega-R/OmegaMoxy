package com.omegar.mvp.compiler.reflector

import com.omegar.mvp.ViewStateProvider
import com.omegar.mvp.compiler.MoxyConst
import com.omegar.mvp.compiler.MvpCompiler
import com.omegar.mvp.compiler.pipeline.KotlinFile
import com.omegar.mvp.compiler.pipeline.KotlinFileProcessor
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.util.*
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import kotlin.reflect.KClass

class MoxyReflectorProcessor(
    private val destinationPackage: String
) : KotlinFileProcessor<Triple<Set<TypeElement>, Set<TypeElement>, Set<String>>>() {

    companion object {
        private val TYPE_ELEMENT_COMPARATOR = Comparator.comparing { obj: TypeElement -> obj.toString() }

        // KClass<*>
        private val CLASS_WILDCARD_TYPE_NAME: TypeName = KClass::class.asClassName().parameterizedBy(STAR)

        // List<Any>
        private val LIST_ANY: TypeName = LIST.parameterizedBy(ANY)

        // Map<Class<*>, ViewStateProvider>
        private val MAP_CLASS_TO_VIEW_STATE_PROVIDER: TypeName =
            MUTABLE_MAP.parameterizedBy(CLASS_WILDCARD_TYPE_NAME, ViewStateProvider::class.asTypeName())

        // Map<Class<*>, List<Object>>
        private val MAP_CLASS_TO_LIST_OF_OBJECT_TYPE_NAME: TypeName =
            MUTABLE_MAP.parameterizedBy(CLASS_WILDCARD_TYPE_NAME, LIST.parameterizedBy(ANY))

    }

    override fun process(input: Triple<Set<TypeElement>, Set<TypeElement>, Set<String>>): KotlinFile {
        return generate(destinationPackage, input.first, input.second, input.third)
    }

    private fun generate(
        destinationPackage: String,
        presenterClassNames: Set<TypeElement>,
        presentersContainers: Set<TypeElement>,
        additionalMoxyReflectorsPackages: Set<String>
    ): KotlinFile {

        val reflectorsPackages = additionalMoxyReflectorsPackages.toMutableSet()

        // sort to preserve order of statements between compilations
        val presenterBinders: Map<TypeElement, List<TypeElement>> = getPresenterBinders(presentersContainers)

        reflectorsPackages.remove(destinationPackage)

        val classBuilder = TypeSpec.objectBuilder("MoxyReflector")
            .addProperty("sViewStateProviders", MAP_CLASS_TO_VIEW_STATE_PROVIDER, KModifier.PRIVATE)
            .addProperty("sPresenterBinders", MAP_CLASS_TO_LIST_OF_OBJECT_TYPE_NAME, KModifier.PRIVATE)

        (presentersContainers + presenterClassNames).forEach { element ->
            classBuilder.addOriginatingElement(element)
        }

        classBuilder.addInitializerBlock(
            generateStaticInitializer(
                presenterClassNames,
                reflectorsPackages,
                presenterBinders
            )
        )
        if (destinationPackage == MvpCompiler.DEFAULT_MOXY_REFLECTOR_PACKAGE) {
            classBuilder.addFunction(
                FunSpec.builder("getViewState")
                    .returns(ANY.copy(nullable = true))
                    .addParameter("presenterClass", CLASS_WILDCARD_TYPE_NAME)
                    .addStatement(
                        "return sViewStateProviders[presenterClass]?.viewState"
                    )
                    .build()
            )
            classBuilder.addFunction(
                FunSpec.builder("getPresenterBinders")
                    .returns(LIST.parameterizedBy(ANY).copy(nullable = true))
                    .addParameter("delegated", CLASS_WILDCARD_TYPE_NAME)
                    .addStatement("return sPresenterBinders[delegated]")
                    .build()
            )
        } else {
            classBuilder.addFunction(
                FunSpec.builder("getViewStateProviders")
                    .returns(MAP_CLASS_TO_VIEW_STATE_PROVIDER)
                    .addStatement("return sViewStateProviders")
                    .build()
            )
            classBuilder.addFunction(
                FunSpec.builder("getPresenterBinders")
                    .returns(MAP_CLASS_TO_LIST_OF_OBJECT_TYPE_NAME)
                    .addStatement("return sPresenterBinders")
                    .build()
            )
        }
        return FileSpec.builder(destinationPackage, "MoxyReflector")
            .addType(classBuilder.build())
            .indent("\t")
            .build()
    }

    private fun generateStaticInitializer(
        presenterClassNamesSet: Set<TypeElement>,
        additionalMoxyReflectorsPackagesSet: Set<String>,
        presenterBinders: Map<TypeElement, List<TypeElement>>
    ): CodeBlock {
        // sort to preserve order of statements between compilations
        val presenterClassNames = presenterClassNamesSet.sortedWith(TYPE_ELEMENT_COMPARATOR)
        val additionalMoxyReflectorsPackages = additionalMoxyReflectorsPackagesSet.sortedWith(Comparator.naturalOrder())
        val builder = CodeBlock.builder()
        builder.addStatement("sViewStateProviders = mutableMapOf(")
        for (presenter in presenterClassNames) {
            val presenterClassName = presenter.asClassName()
            val viewStateProvider = ClassName(
                presenterClassName.packageName,
                presenterClassName.simpleNames.joinToString("$") + MoxyConst.VIEW_STATE_PROVIDER_SUFFIX
            )
            builder.addStatement("\t%T::class to %T(),", presenterClassName, viewStateProvider)
        }
        builder.addStatement(")")

        builder.add("\n")

        builder.addStatement("sPresenterBinders = mutableMapOf(")

        for ((key, value) in presenterBinders) {
            builder.add("\t%T::class to listOf(", key)
            var isFirst = true
            for (typeElement in value) {
                val className = typeElement.asClassName()
                val presenterBinderName =
                    className.simpleNames.joinToString("$") + MoxyConst.PRESENTER_BINDER_SUFFIX
                if (isFirst) {
                    isFirst = false
                } else {
                    builder.add(", ")
                }
                builder.add("%T()", ClassName(className.packageName, presenterBinderName))
            }
            builder.add("),")
        }
        builder.addStatement(")")

        builder.add("\n")
        for (pkg in additionalMoxyReflectorsPackages) {
            val moxyReflector = ClassName(pkg, "MoxyReflector")
            builder.add("\n")
            builder.addStatement("sViewStateProviders.putAll(%T.getViewStateProviders())", moxyReflector)
            builder.addStatement("sPresenterBinders.putAll(%T.getPresenterBinders())", moxyReflector)
        }
        return builder.build()
    }


    /**
     * Collects presenter binders from superclasses that are also presenter containers.
     *
     * @return sorted map between presenter container and list of corresponding binders
     */
    private fun getPresenterBinders(presentersContainers: Set<TypeElement>): SortedMap<TypeElement, List<TypeElement>> {
        val extendingMap: MutableMap<TypeElement, TypeElement?> = HashMap()
        for (presentersContainer in presentersContainers) {
            var superclass = presentersContainer.superclass
            var parent: TypeElement? = null
            while (superclass.kind == TypeKind.DECLARED) {
                val superclassElement = (superclass as DeclaredType).asElement() as TypeElement
                if (presentersContainers.contains(superclassElement)) {
                    parent = superclassElement
                    break
                }
                superclass = superclassElement.superclass
            }
            extendingMap[presentersContainer] = parent
        }

        // TreeMap for sorting
        val elementListMap: SortedMap<TypeElement, List<TypeElement>> = TreeMap(TYPE_ELEMENT_COMPARATOR)
        for (presentersContainer in presentersContainers) {
            val typeElements = ArrayList<TypeElement>()
            typeElements.add(presentersContainer)
            var key = presentersContainer
            while (extendingMap[key]?.also { key = it } != null) {
                typeElements.add(key)
            }
            elementListMap[presentersContainer] = typeElements
        }
        return elementListMap
    }

}