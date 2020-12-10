package com.omegar.mvp.compiler.entity;

import com.squareup.javapoet.ClassName;

import java.util.List;

import javax.lang.model.element.TypeElement;

public class TargetClassInfo implements TypeElementHolder {
	private final ClassName name;
	private final TypeElement element;
	private final List<TargetPresenterField> fields;

	public TargetClassInfo(TypeElement element, List<TargetPresenterField> fields) {
		this.element = element;
		this.name = ClassName.get(element);
		this.fields = fields;
	}

	@Override
	public TypeElement getTypeElement() {
		return element;
	}

	public ClassName getName() {
		return name;
	}

	public List<TargetPresenterField> getFields() {
		return fields;
	}
}
