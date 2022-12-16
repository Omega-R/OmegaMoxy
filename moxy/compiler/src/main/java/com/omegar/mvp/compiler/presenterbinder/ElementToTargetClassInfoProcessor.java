package com.omegar.mvp.compiler.presenterbinder;

import com.omegar.mvp.compiler.entity.PresenterProviderMethod;
import com.omegar.mvp.compiler.entity.TagProviderMethod;
import com.omegar.mvp.compiler.entity.TargetClassInfo;
import com.omegar.mvp.compiler.entity.TargetPresenterField;
import com.omegar.mvp.compiler.pipeline.ElementProcessor;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.presenter.InjectPresenter;
import com.omegar.mvp.presenter.ProvidePresenter;
import com.omegar.mvp.presenter.ProvidePresenterTag;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ElementToTargetClassInfoProcessor extends ElementProcessor<TypeElement, com.omegar.mvp.compiler.entity.TargetClassInfo> {
	private static final String PRESENTER_FIELD_ANNOTATION = InjectPresenter.class.getName();
	private static final String PROVIDE_PRESENTER_ANNOTATION = ProvidePresenter.class.getName();
	private static final String PROVIDE_PRESENTER_TAG_ANNOTATION = ProvidePresenterTag.class.getName();

	@Override
	protected com.omegar.mvp.compiler.entity.TargetClassInfo process(TypeElement presentersContainer) {
		// gather presenter fields info
		List<com.omegar.mvp.compiler.entity.TargetPresenterField> fields = collectFields(presentersContainer);
		bindProvidersToFields(fields, collectPresenterProviders(presentersContainer));
		bindTagProvidersToFields(fields, collectTagProviders(presentersContainer));

		return new TargetClassInfo(presentersContainer, fields);
	}

	private static List<com.omegar.mvp.compiler.entity.TargetPresenterField> collectFields(TypeElement presentersContainer) {
		List<com.omegar.mvp.compiler.entity.TargetPresenterField> fields = new ArrayList<>();

		for (Element element : presentersContainer.getEnclosedElements()) {
			if (element.getKind() != ElementKind.FIELD) {
				continue;
			}

			AnnotationMirror annotation = Util.getAnnotation(element, PRESENTER_FIELD_ANNOTATION);

			if (annotation == null) {
				continue;
			}

			// TODO: simplify?
			TypeMirror clazz = ((DeclaredType) element.asType()).asElement().asType();

			String name = element.toString();

			String type = Util.getAnnotationValueAsString(annotation, "type");

			com.omegar.mvp.compiler.entity.TargetPresenterField field = new com.omegar.mvp.compiler.entity.TargetPresenterField(clazz, name, type);
			fields.add(field);
		}
		return fields;
	}

	private static List<com.omegar.mvp.compiler.entity.PresenterProviderMethod> collectPresenterProviders(TypeElement presentersContainer) {
		List<com.omegar.mvp.compiler.entity.PresenterProviderMethod> providers = new ArrayList<>();

		for (Element element : presentersContainer.getEnclosedElements()) {
			if (element.getKind() != ElementKind.METHOD) {
				continue;
			}

			final ExecutableElement providerMethod = (ExecutableElement) element;

			final AnnotationMirror annotation = Util.getAnnotation(element, PROVIDE_PRESENTER_ANNOTATION);

			if (annotation == null) {
				continue;
			}

			final String name = providerMethod.getSimpleName().toString();
			final DeclaredType kind = ((DeclaredType) providerMethod.getReturnType());

			String type = Util.getAnnotationValueAsString(annotation, "type");
			String tag = Util.getAnnotationValueAsString(annotation, "tag");
			String presenterId = Util.getAnnotationValueAsString(annotation, "presenterId");

			providers.add(new com.omegar.mvp.compiler.entity.PresenterProviderMethod(kind, name, type, tag, presenterId));
		}
		return providers;
	}

	private static List<com.omegar.mvp.compiler.entity.TagProviderMethod> collectTagProviders(TypeElement presentersContainer) {
		List<com.omegar.mvp.compiler.entity.TagProviderMethod> providers = new ArrayList<>();

		for (Element element : presentersContainer.getEnclosedElements()) {
			if (element.getKind() != ElementKind.METHOD) {
				continue;
			}

			final ExecutableElement providerMethod = (ExecutableElement) element;

			final AnnotationMirror annotation = Util.getAnnotation(element, PROVIDE_PRESENTER_TAG_ANNOTATION);

			if (annotation == null) {
				continue;
			}

			final String name = providerMethod.getSimpleName().toString();

			TypeMirror presenterClass = Util.getAnnotationValueAsTypeMirror(annotation, "presenterClass");
			String type = Util.getAnnotationValueAsString(annotation, "type");
			String presenterId = Util.getAnnotationValueAsString(annotation, "presenterId");

			providers.add(new com.omegar.mvp.compiler.entity.TagProviderMethod(presenterClass, name, type, presenterId));
		}
		return providers;
	}

	private static void bindProvidersToFields(List<com.omegar.mvp.compiler.entity.TargetPresenterField> fields,
	                                          List<com.omegar.mvp.compiler.entity.PresenterProviderMethod> presenterProviders) {
		if (fields.isEmpty() || presenterProviders.isEmpty()) {
			return;
		}

		for (PresenterProviderMethod presenterProvider : presenterProviders) {
			TypeMirror providerTypeMirror = presenterProvider.getClazz().asElement().asType();

			for (com.omegar.mvp.compiler.entity.TargetPresenterField field : fields) {
				if ((field.getClazz()).equals(providerTypeMirror)) {
					if (field.getPresenterType() != presenterProvider.getPresenterType()) {
						continue;
					}

					field.setPresenterProviderMethodName(presenterProvider.getName());
				}
			}

		}
	}

	private static void bindTagProvidersToFields(List<com.omegar.mvp.compiler.entity.TargetPresenterField> fields,
	                                             List<com.omegar.mvp.compiler.entity.TagProviderMethod> tagProviders) {
		if (fields.isEmpty() || tagProviders.isEmpty()) {
			return;
		}

		for (TagProviderMethod tagProvider : tagProviders) {
			for (TargetPresenterField field : fields) {
				if ((field.getClazz()).equals(tagProvider.getPresenterClass())) {
					if (field.getPresenterType() != tagProvider.getType()) {
						continue;
					}

					field.setPresenterTagProviderMethodName(tagProvider.getMethodName());
				}
			}
		}
	}

}
