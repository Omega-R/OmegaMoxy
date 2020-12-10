package com.omegar.mvp.compiler;

import com.google.auto.service.AutoService;
import com.omegar.mvp.RegisterMoxyReflectorPackages;
import com.omegar.mvp.compiler.entity.AnnotationInfo;
import com.omegar.mvp.compiler.entity.AnnotationTarget;
import com.omegar.mvp.compiler.pipeline.ElementsAnnotatedGenerator;
import com.omegar.mvp.compiler.pipeline.JavaFileWriter;
import com.omegar.mvp.compiler.pipeline.Pipeline;
import com.omegar.mvp.compiler.pipeline.Publisher;
import com.omegar.mvp.compiler.pipeline.QuadPublisher;
import com.omegar.mvp.compiler.presenterbinder.ElementToTargetClassInfoProcessor;
import com.omegar.mvp.compiler.presenterbinder.TargetClassInfoToPresenterBinderJavaFileProcessor;
import com.omegar.mvp.compiler.presenterbinder.VariableToContainerElementProcessor;
import com.omegar.mvp.compiler.reflector.MoxyReflectorProcessor;
import com.omegar.mvp.compiler.viewstate.ElementToViewInterfaceInfoProcessor;
import com.omegar.mvp.compiler.viewstate.ViewInterfaceInfoToViewStateJavaFileProcessor;
import com.omegar.mvp.compiler.viewstateprovider.ElementToPresenterInfoProcessor;
import com.omegar.mvp.compiler.viewstateprovider.NormalPresenterValidator;
import com.omegar.mvp.compiler.viewstateprovider.PresenterInfoToViewStateProviderJavaFileProcessor;
import com.omegar.mvp.presenter.InjectPresenter;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static javax.lang.model.SourceVersion.latestSupported;

/**
 * Date: 12.12.2015
 * Time: 15:35
 *
 * @author Yuri Shmakov
 */

@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)
public class MvpCompiler extends AbstractProcessor {
	public static final String DEFAULT_MOXY_REFLECTOR_PACKAGE = "com.omegar.mvp";
	private static final String MOXY_ANNOTATION_INJECT_VIEW_STATE = "com.omegar.mvp.InjectViewState";
	private static final String OPTION_MOXY_REFLECTOR_PACKAGE = "moxyReflectorPackage";
	private static final String OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES = "moxyRegisterReflectorPackages";
	private static final String OPTION_ENABLE_ISOLATING_PROCESSING = "moxyEnableIsolatingProcessing";

	private Messager mMessager;
	private Types mTypes;
	private Elements mElements;
	private Map<String, String> mOptions;

	private AnnotationInfo<TypeElement> mInjectViewStateAnnotation;
	private AnnotationInfo<VariableElement> mInjectPresenterAnnotation;

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		mMessager = processingEnv.getMessager();
		mTypes = processingEnv.getTypeUtils();
		mElements = processingEnv.getElementUtils();
		mOptions = processingEnv.getOptions();

		mInjectViewStateAnnotation = AnnotationInfo.create(mElements, MOXY_ANNOTATION_INJECT_VIEW_STATE, AnnotationTarget.CLASS);
		mInjectPresenterAnnotation = AnnotationInfo.create(mElements, InjectPresenter.class, AnnotationTarget.FIELD);
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Util.newHashSet(
				OPTION_MOXY_REFLECTOR_PACKAGE,
				OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES,
				OPTION_ENABLE_ISOLATING_PROCESSING,
				getCurrentIncrementalAnnotationProcessorType().getProcessorOption()
		);
	}

	private boolean isEnabledIsolatingProcessing() {
		return Boolean.parseBoolean(mOptions.get(OPTION_ENABLE_ISOLATING_PROCESSING));
	}

	private IncrementalAnnotationProcessorType getCurrentIncrementalAnnotationProcessorType() {
		return isEnabledIsolatingProcessing()
				? IncrementalAnnotationProcessorType.ISOLATING
				: IncrementalAnnotationProcessorType.AGGREGATING;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Util.newHashSet(
				InjectPresenter.class.getCanonicalName(),
				MOXY_ANNOTATION_INJECT_VIEW_STATE,
				RegisterMoxyReflectorPackages.class.getCanonicalName()
		);
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return latestSupported();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (annotations.isEmpty()) {
			return false;
		}

		try {
			return throwableProcess(annotations, roundEnv);
		} catch (RuntimeException e) {
			mMessager.printMessage(Diagnostic.Kind.OTHER, "Moxy compilation failed. Could you copy stack trace above and write us (or make issue on Github)?");
			e.printStackTrace();
			return true;
		}
	}

	private boolean throwableProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

		String currentMoxyReflectorPackage = mOptions.getOrDefault(OPTION_MOXY_REFLECTOR_PACKAGE, DEFAULT_MOXY_REFLECTOR_PACKAGE);


		Publisher<TypeElement> presenterContainerElementPublisher = new Publisher<>();
		Publisher<TypeElement> presenterElementPublisher = new Publisher<>();
		Publisher<TypeElement> viewElementPublisher = new Publisher<>();
		Publisher<TypeElement> strategiesElementPublisher = new Publisher<>();
		Publisher<String> reflectorPackagesPublisher = new Publisher<>(getAdditionalMoxyReflectorPackages(roundEnv));

		JavaFileWriter fileWriter = new JavaFileWriter(processingEnv.getFiler());

		if (mInjectPresenterAnnotation.contains(annotations)) {
			checkInjectors(roundEnv, new PresenterInjectorRules(mElements, mMessager, mInjectPresenterAnnotation, Modifier.PUBLIC, Modifier.PROTECTED, Modifier.DEFAULT));

			// presenterBinderPipeline
			new Pipeline.Builder<>(new ElementsAnnotatedGenerator<>(roundEnv, mMessager, mInjectPresenterAnnotation))
					.addProcessor(new VariableToContainerElementProcessor())
					.uniqueFilter()
					.copyPublishTo(presenterContainerElementPublisher)
					.addProcessor(new ElementToTargetClassInfoProcessor())
					.addProcessor(new TargetClassInfoToPresenterBinderJavaFileProcessor())
					.buildPipeline(fileWriter)
					.start();
		} else {
			presenterContainerElementPublisher.finish();
		}

		// viewStateProviderPipeline
		new Pipeline.Builder<>(new ElementsAnnotatedGenerator<>(roundEnv, mMessager, mInjectViewStateAnnotation))
				.addProcessor(new ElementToPresenterInfoProcessor(mElements, viewElementPublisher))
				.addValidator(new NormalPresenterValidator())
				.copyTypeElementTo(presenterElementPublisher)
				.addProcessor(new PresenterInfoToViewStateProviderJavaFileProcessor().withCache())
				.buildPipeline(fileWriter)
				.start();

		// viewStatePipeline
		new Pipeline.Builder<>(viewElementPublisher)
				.addProcessor(new ElementToViewInterfaceInfoProcessor(mElements, mTypes, mMessager, strategiesElementPublisher))
				.uniqueFilter()
				.addProcessor(new ViewInterfaceInfoToViewStateJavaFileProcessor(mElements, mTypes, currentMoxyReflectorPackage, reflectorPackagesPublisher))
				.buildPipeline(fileWriter)
				.start();


		// moxyReflectorPipeline
		new Pipeline.Builder<>(
				QuadPublisher.collectQuad(
						presenterElementPublisher,
						presenterContainerElementPublisher,
						strategiesElementPublisher,
						reflectorPackagesPublisher))
				.addProcessor(new MoxyReflectorProcessor(currentMoxyReflectorPackage))
				.buildPipeline(fileWriter)
				.start();


		return true;
	}

	private List<String> getAdditionalMoxyReflectorPackages(RoundEnvironment roundEnv) {
		List<String> result = new ArrayList<>();
		result.addAll(getOptionsAdditionalReflectorPackages());
		result.addAll(getRegisterAdditionalMoxyReflectorPackages(roundEnv));
		return result;
	}

	private List<String> getRegisterAdditionalMoxyReflectorPackages(RoundEnvironment roundEnv) {
		for (Element element : roundEnv.getElementsAnnotatedWith(RegisterMoxyReflectorPackages.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				mMessager.printMessage(Diagnostic.Kind.ERROR, element + " must be " + ElementKind.CLASS.name()
						+ ", or not mark it as @" + RegisterMoxyReflectorPackages.class.getSimpleName());
			}

			String[] packages = element.getAnnotation(RegisterMoxyReflectorPackages.class).value();
			return Arrays.asList(packages);
		}

		return Collections.emptyList();
	}

	private List<String> getOptionsAdditionalReflectorPackages() {
		String moxyRegisterReflectorPackage = mOptions.get(OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES);

		if (moxyRegisterReflectorPackage != null) {
			String[] strings = moxyRegisterReflectorPackage.split(",");
			return Arrays.asList(strings);
		}
		return Collections.emptyList();
	}


	private void checkInjectors(final RoundEnvironment roundEnv, AnnotationRule annotationRule) {
		TypeElement annotationTypeElement = annotationRule.mAnnotationInfo.getAnnotationTypeElement();
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(annotationTypeElement)) {
			annotationRule.checkAnnotation(annotatedElement);
		}

		String errorStack = annotationRule.getErrorStack();
		if (errorStack != null && errorStack.length() > 0) {
			mMessager.printMessage(Diagnostic.Kind.ERROR, errorStack);
		}
	}

}
