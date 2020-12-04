package com.omegar.mvp.compiler.viewstateprovider;

import com.squareup.javapoet.ClassName;

import javax.lang.model.element.TypeElement;

/**
 * Date: 27-Jul-2017
 * Time: 11:55
 *
 * @author Evgeny Kursakov
 */
class PresenterInfo {
	private final ClassName name;
	private final TypeElement element;
	private final ClassName viewStateName;

	PresenterInfo(TypeElement name, String viewStateName) {
		this.name = ClassName.get(name);
		element = name;
		this.viewStateName = ClassName.bestGuess(viewStateName);
	}

	TypeElement getElement() {
		return element;
	}

	ClassName getName() {
		return name;
	}

	ClassName getViewStateName() {
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
