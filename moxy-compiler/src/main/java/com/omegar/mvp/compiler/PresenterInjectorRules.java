package com.omegar.mvp.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.omegar.mvp.MvpPresenter;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.compiler.entity.AnnotationInfo;
import com.omegar.mvp.presenter.InjectPresenter;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;


import static com.omegar.mvp.compiler.Util.fillGenerics;

/**
 * Date: 17-Feb-16
 * Time: 17:00
 *
 * @author esorokin
 */
public class PresenterInjectorRules extends AnnotationRule {

	private final Elements mElements;
	private final Messager mMessager;

	public PresenterInjectorRules(Elements elements, Messager messager, AnnotationInfo<?> annotationInfo, Modifier... validModifiers) {
		super(annotationInfo, validModifiers);
		mElements = elements;
		mMessager = messager;
	}

	@SuppressWarnings("StringConcatenationInsideStringBufferAppend")
	@Override
	public void checkAnnotation(Element annotatedField) {
		checkEnvironment(annotatedField);

		ElementKind validKind = mAnnotationInfo.getElementKind();

		if (annotatedField.getKind() != validKind) {
			mErrorBuilder.append("Field " + annotatedField + " of " + annotatedField.getEnclosingElement().getSimpleName() + " should be " + validKind.name() + ", or not mark it as @" + InjectPresenter.class.getSimpleName()).append("\n");
		}

		for (Modifier modifier : annotatedField.getModifiers()) {
			if (!mValidModifiers.contains(modifier)) {
				mErrorBuilder.append("Field " + annotatedField + " of " + annotatedField.getEnclosingElement().getSimpleName() + " can't be a " + modifier).append(". Use ").append(validModifiersToString()).append("\n");
			}
		}

		Element enclosingElement = annotatedField.getEnclosingElement();
		while (enclosingElement.getKind() == ElementKind.CLASS) {
			if (!enclosingElement.getModifiers().contains(Modifier.PUBLIC)) {
				mErrorBuilder.append(enclosingElement.getSimpleName() + " should be PUBLIC ");
				break;
			}

			enclosingElement = enclosingElement.getEnclosingElement();
		}
	}

	private void checkEnvironment(final Element annotatedField) {
		if (!(annotatedField.asType() instanceof DeclaredType)) {
			return;
		}

		TypeElement typeElement = (TypeElement) ((DeclaredType) annotatedField.asType()).asElement();
		String viewClassFromGeneric = getViewClassFromGeneric(typeElement, (DeclaredType) annotatedField.asType());

		Collection<TypeMirror> viewsType = getViewsType((TypeElement) ((DeclaredType) annotatedField.getEnclosingElement().asType()).asElement());

		boolean result = false;

		for (TypeMirror typeMirror : viewsType) {
			if (Util.getFullClassName(mElements, typeMirror).equals(viewClassFromGeneric) || Util.fillGenerics(Collections.<String, String>emptyMap(), typeMirror).equals(viewClassFromGeneric)) {
				result = true;
				break;
			}
		}
		if (!result) {
			mMessager.printMessage(Diagnostic.Kind.ERROR, "You can not use @InjectPresenter in classes that are not View, which is typified target Presenter", annotatedField);
		}
	}

	private String getViewClassFromGeneric(TypeElement typeElement, DeclaredType declaredType) {
		TypeMirror superclass = declaredType;
		Map<TypeParameterElement, TypeMirror> mTypedMap = Collections.emptyMap();

		if (!typeElement.getTypeParameters().isEmpty()) {
			mTypedMap = getChildInstanceOfClassFromGeneric(typeElement, MvpView.class);
		}


		Map<String, String> parentTypes = Collections.emptyMap();
		List<? extends TypeMirror> totalTypeArguments = new ArrayList<>(((DeclaredType) superclass).getTypeArguments());
		while (superclass.getKind() != TypeKind.NONE) {
			TypeElement superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();
			List<? extends TypeMirror> typeArguments = ((DeclaredType) superclass).getTypeArguments();
			totalTypeArguments.retainAll(typeArguments);
			final List<? extends TypeParameterElement> typeParameters = superclassElement.getTypeParameters();

			Map<String, String> types = new HashMap<>();
			for (int i = 0; i < typeArguments.size(); i++) {
				types.put(typeParameters.get(i).toString(), Util.fillGenerics(parentTypes, typeArguments.get(i)));
			}

			if (superclassElement.toString().equals(MvpPresenter.class.getCanonicalName())) {
				if (!typeArguments.isEmpty()) {
					TypeMirror typeMirror = typeArguments.get(0);
					if (typeMirror instanceof TypeVariable) {
						Element key = ((TypeVariable) typeMirror).asElement();

						for (Map.Entry<TypeParameterElement, TypeMirror> entry : mTypedMap.entrySet()) {
							if (entry.getKey().toString().equals(key.toString())) {
								return Util.getFullClassName(mElements, entry.getValue());
							}
						}
					}
				}

				if (typeArguments.isEmpty() && typeParameters.isEmpty()) {
					return ((DeclaredType) superclass).asElement().getSimpleName().toString();
				}
				// MvpPresenter is typed only on View class
				return Util.fillGenerics(parentTypes, typeArguments);
			}

			parentTypes = types;

			superclass = superclassElement.getSuperclass();
		}

		return "";
	}

	private Map<TypeParameterElement, TypeMirror> getChildInstanceOfClassFromGeneric(final TypeElement typeElement, final Class<?> aClass) {
		Map<TypeParameterElement, TypeMirror> result = new HashMap<>();
		for (TypeParameterElement element : typeElement.getTypeParameters()) {
			List<? extends TypeMirror> bounds = element.getBounds();
			for (TypeMirror bound : bounds) {
				if (bound instanceof DeclaredType && ((DeclaredType) bound).asElement() instanceof TypeElement) {
					Collection<TypeMirror> viewsType = getViewsType((TypeElement) ((DeclaredType) bound).asElement());
					boolean isViewType = false;
					for (TypeMirror viewType : viewsType) {
						if (((DeclaredType) viewType).asElement().toString().equals(aClass.getCanonicalName())) {
							isViewType = true;
						}
					}

					if (isViewType) {
						result.put(element, bound);
						break;
					}
				}
			}
		}

		return result;
	}

	private Collection<TypeMirror> getViewsType(TypeElement typeElement) {
		TypeMirror superclass = typeElement.asType();

		List<TypeMirror> result = new ArrayList<>();

		while (superclass.getKind() != TypeKind.NONE) {
			TypeElement superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();
			Collection<? extends TypeMirror> interfaces = new HashSet<>(superclassElement.getInterfaces());
			for (TypeMirror typeMirror : interfaces) {
				if (typeMirror instanceof DeclaredType) {
					result.addAll(getViewsType((TypeElement) ((DeclaredType) typeMirror).asElement()));
				}
			}
			result.addAll(interfaces);
			result.add(superclass);

			superclass = superclassElement.getSuperclass();
		}

		return result;
	}
}
