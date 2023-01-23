package com.omegar.mvp.compiler

import com.omegar.mvp.MvpPresenter
import com.omegar.mvp.MvpView
import com.omegar.mvp.compiler.entity.AnnotationInfo
import com.omegar.mvp.presenter.InjectPresenter
import javax.annotation.processing.Messager
import javax.lang.model.element.*
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeKind
import javax.lang.model.type.TypeMirror
import javax.lang.model.type.TypeVariable
import javax.lang.model.util.Elements
import javax.tools.Diagnostic

/**
 * Date: 17-Feb-16
 * Time: 17:00
 *
 * @author esorokin
 */
class PresenterInjectorRules(
    private val elements: Elements,
    private val messager: Messager,
    annotationInfo: AnnotationInfo<*>,
    vararg validModifiers: Modifier
) : AnnotationRule(annotationInfo, *validModifiers) {

    override fun checkAnnotation(annotatedField: Element) {
        checkEnvironment(annotatedField)
        val validKind = annotationInfo.elementKind
        if (annotatedField.kind != validKind) {
            errorBuilder.append("Field " + annotatedField + " of " + annotatedField.enclosingElement.simpleName + " should be " + validKind.name + ", or not mark it as @" + InjectPresenter::class.java.simpleName)
                .append("\n")
        }
        for (modifier in annotatedField.modifiers) {
            if (!validModifiers.contains(modifier)) {
                errorBuilder.append("Field " + annotatedField + " of " + annotatedField.enclosingElement.simpleName + " can't be a " + modifier)
                    .append(". Use ").append(validModifiersToString()).append("\n")
            }
        }
        var enclosingElement = annotatedField.enclosingElement
        while (enclosingElement.kind == ElementKind.CLASS) {
            if (!enclosingElement.modifiers.contains(Modifier.PUBLIC)) {
                errorBuilder.append(enclosingElement.simpleName.toString() + " should be PUBLIC ")
                break
            }
            enclosingElement = enclosingElement.enclosingElement
        }
    }

    private fun checkEnvironment(annotatedField: Element?) {
        if (annotatedField!!.asType() !is DeclaredType) {
            return
        }
        val typeElement = (annotatedField.asType() as DeclaredType).asElement() as TypeElement
        val viewClassFromGeneric = getViewClassFromGeneric(typeElement, annotatedField.asType() as DeclaredType)
        val viewsType = getViewsType((annotatedField.enclosingElement.asType() as DeclaredType).asElement() as TypeElement)
        var result = false
        for (typeMirror in viewsType) {
            if (Util.getFullClassName(elements, typeMirror) == viewClassFromGeneric || Util.fillGenerics(
                    emptyMap(),
                    typeMirror
                ) == viewClassFromGeneric
            ) {
                result = true
                break
            }
        }
        if (!result) {
            messager.printMessage(
                Diagnostic.Kind.ERROR,
                "You can not use @InjectPresenter in classes that are not View, which is typified target Presenter",
                annotatedField
            )
        }
    }

    private fun getViewClassFromGeneric(typeElement: TypeElement, declaredType: DeclaredType): String {
        var superclass: TypeMirror = declaredType
        var typedMap: Map<TypeParameterElement, TypeMirror?> = emptyMap<TypeParameterElement, TypeMirror>()
        if (typeElement.typeParameters.isNotEmpty()) {
            typedMap = getChildInstanceOfClassFromGeneric(typeElement, MvpView::class.java)
        }
        var parentTypes: Map<String, String> = emptyMap()
        val totalTypeArguments = ArrayList((superclass as DeclaredType).typeArguments)
        while (superclass.kind != TypeKind.NONE) {
            val superclassElement = (superclass as DeclaredType).asElement() as TypeElement
            val typeArguments = superclass.typeArguments
            totalTypeArguments.retainAll(typeArguments.toSet())
            val typeParameters = superclassElement.typeParameters
            val types: MutableMap<String, String> = HashMap()
            for (i in typeArguments.indices) {
                types[typeParameters[i].toString()] = Util.fillGenerics(parentTypes, typeArguments[i])
            }
            if (superclassElement.toString() == MvpPresenter::class.java.canonicalName) {
                if (typeArguments.isNotEmpty()) {
                    val typeMirror = typeArguments[0]
                    if (typeMirror is TypeVariable) {
                        val key = typeMirror.asElement()
                        for ((key1, value) in typedMap) {
                            if (key1.toString() == key.toString()) {
                                return Util.getFullClassName(elements, value)
                            }
                        }
                    }
                }
                return if (typeArguments.isEmpty() && typeParameters.isEmpty()) {
                    superclass.asElement().simpleName.toString()
                } else Util.fillGenerics(parentTypes, typeArguments)
                // MvpPresenter is typed only on View class
            }
            parentTypes = types
            superclass = superclassElement.superclass
        }
        return ""
    }

    private fun getChildInstanceOfClassFromGeneric(
        typeElement: TypeElement,
        aClass: Class<*>
    ): Map<TypeParameterElement, TypeMirror?> {
        val result: MutableMap<TypeParameterElement, TypeMirror?> = HashMap()
        for (element in typeElement.typeParameters) {
            val bounds = element.bounds
            for (bound in bounds) {
                if (bound is DeclaredType && bound.asElement() is TypeElement) {
                    val viewsType = getViewsType(bound.asElement() as TypeElement)
                    var isViewType = false
                    for (viewType in viewsType) {
                        if ((viewType as DeclaredType).asElement().toString() == aClass.canonicalName) {
                            isViewType = true
                        }
                    }
                    if (isViewType) {
                        result[element] = bound
                        break
                    }
                }
            }
        }
        return result
    }

    private fun getViewsType(typeElement: TypeElement): Collection<TypeMirror> {
        var superclass = typeElement.asType()
        val result: MutableList<TypeMirror> = ArrayList()
        while (superclass.kind != TypeKind.NONE) {
            val superclassElement = (superclass as DeclaredType).asElement() as TypeElement
            val interfaces: Collection<TypeMirror> = HashSet(superclassElement.interfaces)
            for (typeMirror in interfaces) {
                if (typeMirror is DeclaredType) {
                    result.addAll(getViewsType(typeMirror.asElement() as TypeElement))
                }
            }
            result.addAll(interfaces)
            result.add(superclass)
            superclass = superclassElement.superclass
        }
        return result
    }
}