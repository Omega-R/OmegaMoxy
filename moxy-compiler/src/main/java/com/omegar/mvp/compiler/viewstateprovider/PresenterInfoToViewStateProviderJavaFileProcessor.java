package com.omegar.mvp.compiler.viewstateprovider;

import com.omegar.mvp.MvpProcessor;
import com.omegar.mvp.MvpView;
import com.omegar.mvp.ViewStateProvider;
import com.omegar.mvp.compiler.entity.PresenterInfo;
import com.omegar.mvp.compiler.pipeline.JavaFileProcessor;
import com.omegar.mvp.viewstate.MvpViewState;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import javax.lang.model.element.Modifier;

/**
 * Date: 19-Jan-16
 * Time: 19:51
 *
 * @author Alexander Blinov
 */
public final class PresenterInfoToViewStateProviderJavaFileProcessor extends JavaFileProcessor<com.omegar.mvp.compiler.entity.PresenterInfo> {

	@Override
	public JavaFile process(PresenterInfo presenterInfo) {
		TypeSpec typeSpec = TypeSpec.classBuilder(presenterInfo.getName().simpleName() + MvpProcessor.VIEW_STATE_PROVIDER_SUFFIX)
				.addOriginatingElement(presenterInfo.getTypeElement())
				.addModifiers(Modifier.PUBLIC)
				.superclass(ViewStateProvider.class)
				.addMethod(generateGetViewStateMethod(presenterInfo.getName(), presenterInfo.getViewStateName()))
				.build();

		return JavaFile.builder(presenterInfo.getName().packageName(), typeSpec)
				.indent("\t")
				.build();
	}


	private MethodSpec generateGetViewStateMethod(ClassName presenter, ClassName viewState) {
		MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("getViewState")
				.addAnnotation(Override.class)
				.addModifiers(Modifier.PUBLIC)
				.returns(ParameterizedTypeName.get(ClassName.get(MvpViewState.class), WildcardTypeName.subtypeOf(MvpView.class)));

		if (viewState == null) {
			methodBuilder.addStatement("throw new RuntimeException($S)", presenter.reflectionName() + " should has view");
		} else {
			methodBuilder.addStatement("return new $T()", viewState);
		}

		return methodBuilder.build();
	}
}

