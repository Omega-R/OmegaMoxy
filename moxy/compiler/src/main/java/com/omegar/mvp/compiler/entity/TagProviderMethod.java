package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.presenter.PresenterType;

import javax.lang.model.type.TypeMirror;

public class TagProviderMethod {
	private final TypeMirror presenterClass;
	private final String methodName;
	private final PresenterType type;
	private final String presenterId;

	public TagProviderMethod(TypeMirror presenterClass, String methodName, String type, String presenterId) {
		this.presenterClass = presenterClass;
		this.methodName = methodName;
		if (type == null) {
			this.type = PresenterType.LOCAL;
		} else {
			this.type = PresenterType.valueOf(type);
		}
		this.presenterId = presenterId;
	}

	public TypeMirror getPresenterClass() {
		return presenterClass;
	}

	public String getMethodName() {
		return methodName;
	}

	public PresenterType getType() {
		return type;
	}

	public String getPresenterId() {
		return presenterId;
	}
}
