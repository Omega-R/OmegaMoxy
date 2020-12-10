package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.compiler.MvpCompiler;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

/**
 * Date: 27-Jul-2017
 * Time: 12:58
 *
 * @author Evgeny Kursakov
 */
public class ViewMethod {
	private final ExecutableElement methodElement;
	private final String name;
	private final TypeElement strategy;
	private final String tag;
	private final List<ParameterSpec> parameterSpecs;
	private final List<TypeName> exceptions;
	private final List<TypeVariableName> typeVariables;
	private final String argumentsString;

	private String uniqueSuffix;

	public ViewMethod(Types types, DeclaredType targetInterfaceElement, ViewMethod method) {
        this.methodElement = method.getElement();
        this.name = method.name;
        this.strategy = method.strategy;
        this.tag = method.tag;
        this.parameterSpecs = formatParameters(types, targetInterfaceElement, method.methodElement, method.parameterSpecs);
        this.exceptions = method.exceptions;
        this.typeVariables = method.typeVariables;
        this.argumentsString = method.argumentsString;
        this.uniqueSuffix = method.uniqueSuffix;
    }

	public ViewMethod(Types types, DeclaredType targetInterfaceElement,
	           ExecutableElement methodElement,
	           TypeElement strategy,
	           String tag) {
		this.methodElement = methodElement;
		this.name = methodElement.getSimpleName().toString();
		this.strategy = strategy;
		this.tag = tag;

		this.parameterSpecs = new ArrayList<>();

		ExecutableType executableType = (ExecutableType) types.asMemberOf(targetInterfaceElement, methodElement);
		List<? extends VariableElement> parameters = methodElement.getParameters();
		List<? extends TypeMirror> resolvedParameterTypes = executableType.getParameterTypes();

		for (int i = 0; i < parameters.size(); i++) {
			VariableElement element = parameters.get(i);
			TypeName type = TypeName.get(resolvedParameterTypes.get(i));
			String name = element.getSimpleName().toString();

			parameterSpecs.add(ParameterSpec.builder(type, name)
					.addModifiers(element.getModifiers())
					.build()
			);
		}

		this.exceptions = methodElement.getThrownTypes().stream()
				.map(TypeName::get)
				.collect(Collectors.toList());

		this.typeVariables = methodElement.getTypeParameters()
				.stream()
				.map(TypeVariableName::get)
				.collect(Collectors.toList());

		this.argumentsString = this.parameterSpecs.stream()
				.map(parameterSpec -> parameterSpec.name)
				.collect(Collectors.joining(", "));

		this.uniqueSuffix = "";
	}

	private List<ParameterSpec> formatParameters(Types types, DeclaredType enclosingType, ExecutableElement element,
												 List<ParameterSpec> parameterSpecs) {
		List<ParameterSpec> list = new ArrayList<>();

		ExecutableType executableType = (ExecutableType) types.asMemberOf(enclosingType, element);
		List<? extends TypeMirror> resolvedParameterTypes = executableType.getParameterTypes();

		for (int i = 0; i < parameterSpecs.size(); i++) {
			ParameterSpec parameter = parameterSpecs.get(i);
			TypeName type = TypeName.get(resolvedParameterTypes.get(i));
			list.add(ParameterSpec.builder(type, parameter.name).build());
		}

		return list;
	}

	public ExecutableElement getElement() {
		return methodElement;
	}

	public String getName() {
		return name;
	}

	public TypeElement getStrategy() {
		return strategy;
	}

	public String getTag() {
		return tag;
	}

	public List<ParameterSpec> getParameterSpecs() {
		return parameterSpecs;
	}

	public List<TypeName> getExceptions() {
		return exceptions;
	}

	public List<TypeVariableName> getTypeVariables() {
		return typeVariables;
	}

	public String getArgumentsString() {
		return argumentsString;
	}

	public String getCommandClassName() {
		return name.substring(0, 1).toUpperCase() + name.substring(1) + uniqueSuffix + "Command";
	}

	public String getEnclosedClassName() {
		TypeElement typeElement = (TypeElement) methodElement.getEnclosingElement();
		return typeElement.getQualifiedName().toString();
	}

	public String getUniqueSuffix() {
		return uniqueSuffix;
	}

	public void setUniqueSuffix(String uniqueSuffix) {
		this.uniqueSuffix = uniqueSuffix;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ViewMethod that = (ViewMethod) o;

		if (methodElement != null ? !methodElement.equals(that.methodElement) : that.methodElement != null) return false;
		if (name != null ? !name.equals(that.name) : that.name != null) return false;
		if (tag != null ? !tag.equals(that.tag) : that.tag != null) return false;
		if (argumentsString != null ? !argumentsString.equals(that.argumentsString) : that.argumentsString != null) return false;
		return uniqueSuffix != null ? uniqueSuffix.equals(that.uniqueSuffix) : that.uniqueSuffix == null;
	}

	@Override
	public int hashCode() {
		int result = methodElement != null ? methodElement.hashCode() : 0;
		result = 31 * result + (name != null ? name.hashCode() : 0);
		result = 31 * result + (tag != null ? tag.hashCode() : 0);
		result = 31 * result + (argumentsString != null ? argumentsString.hashCode() : 0);
		result = 31 * result + (uniqueSuffix != null ? uniqueSuffix.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "ViewMethod{" +
				"name='" + name + '\'' +
				'}';
	}
}
