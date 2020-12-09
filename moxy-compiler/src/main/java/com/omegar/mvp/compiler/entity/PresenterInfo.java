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
	private final ClassName mName;
	private final TypeElement mElement;
	private final ClassName mViewStateName;
	private final boolean mIsViewParametrized;
	private final boolean mIsAbstracted;


	public PresenterInfo(TypeElement name, String viewStateName) {
		mName = ClassName.get(name);
		mElement = name;
		mViewStateName = ClassName.bestGuess(viewStateName);
		mIsViewParametrized = TypeName.get(name.asType()) instanceof ParameterizedTypeName;
		mIsAbstracted = name.getModifiers().contains(Modifier.ABSTRACT);
	}

	public boolean isParametrized() {
		return mIsViewParametrized;
	}

	public boolean isAbstracted() {
		return mIsAbstracted;
	}

	@Override
	public TypeElement getTypeElement() {
		return mElement;
	}

	public ClassName getName() {
		return mName;
	}

	public ClassName getViewStateName() {
		return mViewStateName;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PresenterInfo that = (PresenterInfo) o;

		if (mName != null ? !mName.equals(that.mName) : that.mName != null) return false;
		if (mElement != null ? !mElement.equals(that.mElement) : that.mElement != null) return false;
		return mViewStateName != null ? mViewStateName.equals(that.mViewStateName) : that.mViewStateName == null;
	}

	@Override
	public int hashCode() {
		int result = mName != null ? mName.hashCode() : 0;
		result = 31 * result + (mElement != null ? mElement.hashCode() : 0);
		result = 31 * result + (mViewStateName != null ? mViewStateName.hashCode() : 0);
		return result;
	}
}
