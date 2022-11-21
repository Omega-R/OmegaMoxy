package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.presenter.PresenterType;

import javax.lang.model.type.DeclaredType;

public class PresenterProviderMethod {
	private final DeclaredType clazz;
	private final String name;
	private final PresenterType presenterType;
	private final String tag;
	private final String presenterId;

	public PresenterProviderMethod(DeclaredType clazz, String name, String type, String tag, String presenterId) {
		this.clazz = clazz;
		this.name = name;
		if (type == null) {
			presenterType = PresenterType.LOCAL;
		} else {
			presenterType = PresenterType.valueOf(type);
		}
		this.tag = tag;
		this.presenterId = presenterId;
	}

	public DeclaredType getClazz() {
		return clazz;
	}

	public String getName() {
		return name;
	}

	public PresenterType getPresenterType() {
		return presenterType;
	}

	public String getTag() {
		return tag;
	}

	public String getPresenterId() {
		return presenterId;
	}
}
