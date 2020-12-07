package com.omegar.mvp.compiler.entity;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Date: 27-Jul-2017
 * Time: 11:55
 *
 * @author Evgeny Kursakov
 */
public class PresenterInfo implements TypeElementHolder {
	private final ClassName name;
	private final TypeElement element;
	private final ClassName viewStateName;
	private final boolean isViewParametrized;
	private final boolean isAbstracted;


	public PresenterInfo(TypeElement name, String viewStateName) {
		this.name = ClassName.get(name);
		element = name;
		this.viewStateName = ClassName.bestGuess(viewStateName);
		isViewParametrized = TypeName.get(name.asType()) instanceof ParameterizedTypeName;
		isAbstracted = name.getModifiers().contains(Modifier.ABSTRACT);
	}

	public boolean isParametrized() {
		return isViewParametrized;
	}

	public boolean isAbstracted() {
		return isAbstracted;
	}

	@Override
	public TypeElement getTypeElement() {
		return element;
	}

	public ClassName getName() {
		return name;
	}

	public ClassName getViewStateName() {
		return viewStateName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PresenterInfo that = (PresenterInfo) o;

		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (element != null ? !element.equals(that.element) : that.element != null) return false;
		return viewStateName != null ? viewStateName.equals(that.viewStateName) : that.viewStateName == null;
	}

	@Override
	public int hashCode() {
		int result = name != null ? name.hashCode() : 0;
		result = 31 * result + (element != null ? element.hashCode() : 0);
		result = 31 * result + (viewStateName != null ? viewStateName.hashCode() : 0);
		return result;
	}
}
