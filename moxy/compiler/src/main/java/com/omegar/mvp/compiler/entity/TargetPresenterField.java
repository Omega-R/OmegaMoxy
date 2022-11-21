package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.MvpProcessor;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.presenter.PresenterType;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;

import javax.lang.model.type.TypeMirror;

public class TargetPresenterField {
	private final TypeMirror clazz;
	private final boolean isParametrized;
	private final TypeName typeName;
	private final String name;
	private final PresenterType presenterType;
	private final String tag;
	private final String presenterId;

	private String presenterProviderMethodName;
	private String presenterTagProviderMethodName;

	public TargetPresenterField(TypeMirror clazz,
	                     String name,
	                     String presenterType,
	                     String tag,
	                     String presenterId) {
		this.clazz = clazz;
		this.isParametrized = TypeName.get(clazz) instanceof ParameterizedTypeName;
		this.typeName = isParametrized ? ((ParameterizedTypeName) TypeName.get(clazz)).rawType : TypeName.get(clazz);
		this.name = name;
		this.tag = tag;

		if (presenterType == null) {
			this.presenterType = PresenterType.LOCAL;
		} else {
			this.presenterType = PresenterType.valueOf(presenterType);
		}

		this.presenterId = presenterId;
	}

	public boolean isParametrized() {
		return isParametrized;
	}

	public TypeMirror getClazz() {
		return clazz;
	}

	public TypeName getTypeName() {
		return typeName;
	}

	public String getGeneratedClassName() {
		return Util.capitalizeString(name) + MvpProcessor.PRESENTER_BINDER_INNER_SUFFIX;
	}

	public String getTag() {
		return tag;
	}

	public String getName() {
		return name;
	}

	public PresenterType getPresenterType() {
		return presenterType;
	}

	public String getPresenterId() {
		return presenterId;
	}

	public String getPresenterProviderMethodName() {
		return presenterProviderMethodName;
	}

	public void setPresenterProviderMethodName(String presenterProviderMethodName) {
		this.presenterProviderMethodName = presenterProviderMethodName;
	}

	public String getPresenterTagProviderMethodName() {
		return presenterTagProviderMethodName;
	}

	public void setPresenterTagProviderMethodName(String presenterTagProviderMethodName) {
		this.presenterTagProviderMethodName = presenterTagProviderMethodName;
	}
}
