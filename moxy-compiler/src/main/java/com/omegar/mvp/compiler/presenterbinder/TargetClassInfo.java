package com.omegar.mvp.compiler.presenterbinder;

import com.squareup.javapoet.ClassName;

import java.util.List;

import javax.lang.model.element.TypeElement;

class TargetClassInfo {
	private final ClassName name;
	private final TypeElement element;
	private final List<TargetPresenterField> fields;

	TargetClassInfo(TypeElement element, List<TargetPresenterField> fields) {
		this.element = element;
		this.name = ClassName.get(element);
		this.fields = fields;
	}

	TypeElement getElement() {
		return element;
	}

	ClassName getName() {
		return name;
	}

	List<TargetPresenterField> getFields() {
		return fields;
	}
}
