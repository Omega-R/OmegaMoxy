/*
 * Copyright (C) 2013 Google, Inc.
 * Copyright (C) 2013 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.omegar.mvp.compiler;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StrategyType;
import com.squareup.javapoet.ClassName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;

/**
 * Utilities for handling types in annotation processors
 *
 * @author Yuri Shmakov
 */
@SuppressWarnings("WeakerAccess")
public final class Util {

	public static final ClassName MVP_VIEW_CLASS_NAME = ClassName.get(MvpView.class);


	public static String fillGenerics(Map<String, String> types, TypeMirror param) {
		return fillGenerics(types, Collections.singletonList(param));
	}

	public static String fillGenerics(Map<String, String> types, List<? extends TypeMirror> params) {
		return fillGenerics(types, params, ", ");
	}

	public static String fillGenerics(Map<String, String> types, List<? extends TypeMirror> params, String separator) {
		StringBuilder result = new StringBuilder();

		for (TypeMirror param : params) {
			if (result.length() > 0) {
				result.append(separator);
			}

			/**
			 * "if" block's order is critically! E.g. IntersectionType is TypeVariable.
			 */
			if (param instanceof WildcardType) {
				result.append("?");
				final TypeMirror extendsBound = ((WildcardType) param).getExtendsBound();
				if (extendsBound != null) {
					result.append(" extends ").append(fillGenerics(types, extendsBound));
				}
				final TypeMirror superBound = ((WildcardType) param).getSuperBound();
				if (superBound != null) {
					result.append(" super ").append(fillGenerics(types, superBound));
				}
			} else if (param instanceof IntersectionType) {
				result.append("?");
				final List<? extends TypeMirror> bounds = ((IntersectionType) param).getBounds();

				if (!bounds.isEmpty()) {
					result.append(" extends ").append(fillGenerics(types, bounds, " & "));
				}
			} else if (param instanceof DeclaredType) {
				result.append(((DeclaredType) param).asElement());

				final List<? extends TypeMirror> typeArguments = ((DeclaredType) param).getTypeArguments();
				if (!typeArguments.isEmpty()) {
					final String s = fillGenerics(types, typeArguments);

					result.append("<").append(s).append(">");
				}
			} else if (param instanceof TypeVariable) {
				String type = types.get(param.toString());

				if (type == null) {
					type = ((TypeVariable) param).getUpperBound().toString();
				}
				result.append(type);

			} else {
				result.append(param);
			}
		}

		return result.toString();
	}

	public static String getFullClassName(Elements elements, TypeMirror typeMirror) {
		if (!(typeMirror instanceof DeclaredType)) {
			return "";
		}

		TypeElement typeElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
		return getFullClassName(elements, typeElement);
	}

	public static String getFullClassName(Elements elements, TypeElement typeElement) {
		String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
		if (packageName.length() > 0) {
			packageName += ".";
		}

		String className = typeElement.toString().substring(packageName.length());
		return packageName + className.replaceAll("\\.", "\\$");
	}

    public static String getSimpleClassName(Elements elements, TypeElement typeElement) {
        String packageName = elements.getPackageOf(typeElement).getQualifiedName().toString();
        if (packageName.length() > 0) {
            packageName += ".";
        }

        String className = typeElement.toString().substring(packageName.length());
        return className.replaceAll("\\.", "\\$");
    }

	public static AnnotationMirror getAnnotation(Element element, String annotationClass) {
		for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
			if (annotationMirror.getAnnotationType().asElement().toString().equals(annotationClass))
				return annotationMirror;
		}

		return null;
	}

	public static TypeMirror getAnnotationValueAsTypeMirror(AnnotationMirror annotationMirror, String key) {
		AnnotationValue av = getAnnotationValue(annotationMirror, key);

		if (av != null) {
			return (TypeMirror) av.getValue();
		} else {
			return null;
		}
	}

	public static StrategyType getAnnotationValueAsStrategyType(AnnotationMirror annotationMirror, String key) {
		AnnotationValue av = getAnnotationValue(annotationMirror, key);

		if (av != null) {
			String enumString = av.getValue().toString();
			return StrategyType.valueOf(enumString);
		} else {
			return null;
		}
	}

	public static String getAnnotationValueAsString(AnnotationMirror annotationMirror, String key) {
		AnnotationValue av = getAnnotationValue(annotationMirror, key);

		if (av != null) {
			return av.getValue().toString();
		} else {
			return null;
		}
	}

	public static AnnotationValue getAnnotationValue(AnnotationMirror annotationMirror, String key) {
		if (annotationMirror == null) return null;

		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
			if (entry.getKey().getSimpleName().toString().equals(key)) {
				return entry.getValue();
			}
		}

		return null;
	}

	public static Map<String, AnnotationValue> getAnnotationValues(AnnotationMirror annotationMirror) {
		if (annotationMirror == null) return Collections.emptyMap();

		Map<String, AnnotationValue> result = new HashMap<>();

		for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet()) {
			String key = entry.getKey().getSimpleName().toString();
			if (entry.getValue() != null) {
				result.put(key, entry.getValue());
			}
		}

		return result;
	}

	public static boolean hasEmptyConstructor(TypeElement element) {
		for (Element enclosedElement : element.getEnclosedElements()) {
			if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
				List<? extends VariableElement> parameters = ((ExecutableElement) enclosedElement).getParameters();
				if (parameters == null || parameters.isEmpty()) {
					return true;
				}
			}
		}
		return false;
	}

	public static String capitalizeString(String string) {
		return string == null || string.isEmpty() ? "" : string.length() == 1 ? string.toUpperCase() : Character.toUpperCase(string.charAt(0)) + string.substring(1);
	}


	public static String decapitalizeString(String string) {
		return string == null || string.isEmpty() ? "" : string.length() == 1 ? string.toLowerCase() : Character.toLowerCase(string.charAt(0)) + string.substring(1);
	}

	public static boolean isMvpElement(TypeElement element) {
		if (element == null) return false;

		ClassName className = ClassName.get(element);
		if (className.equals(MVP_VIEW_CLASS_NAME)) return true;

		for (TypeMirror typeMirror : element.getInterfaces()) {
			TypeElement interfaceElement = (TypeElement) ((DeclaredType) typeMirror).asElement();
			if (isMvpElement(interfaceElement)) return true;
		}
		return false;
	}

	public static <E> E firstOrNull(@Nullable List<E> list) {
		if (list == null || list.isEmpty()) return null;
		return list.get(0);
	}

	public static <E> E lastOrNull(@Nullable Set<E> set) {
		if (set == null || set.isEmpty()) return null;
		return last(set.iterator());
	}

	public static <E> E lastOrNull(@Nullable List<E> list) {
		if (list == null || list.isEmpty()) return null;
		return list.get(list.size() - 1);
	}

	public static <T> T last(Iterator<T> iterator) {
		while (true) {
			T current = iterator.next();
			if (!iterator.hasNext()) {
				return current;
			}
		}
	}

	public static TypeElement asElement(TypeMirror mirror) {
		return (TypeElement) ((DeclaredType) mirror).asElement();
	}

	public static <E> List<E> newDistinctList(List<E> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}

	public static String substringBefore(String string, char beforeChar) {
		int beforeIndex = string.indexOf(beforeChar);
		if (beforeIndex >= 0) return string.substring(0, beforeIndex); else return string;
	}

	public static <E> HashSet<E> newHashSet(E... elements) {
		HashSet<E> set = new HashSet<>(elements.length);
		Collections.addAll(set, elements);
		return set;
	}

	public static boolean startWith(@Nonnull String string, @Nonnull String prefix) {
		return string.indexOf(prefix) == 0;
	}

}
