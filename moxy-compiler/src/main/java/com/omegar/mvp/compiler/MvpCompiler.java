package com.omegar.mvp.compiler;

import com.google.auto.service.AutoService;
import com.omegar.mvp.RegisterMoxyReflectorPackages;
import com.omegar.mvp.compiler.entity.AnnotationInfo;
import com.omegar.mvp.compiler.entity.AnnotationTarget;
import com.omegar.mvp.compiler.pipeline.ElementByAnnotationGenerator;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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

@SuppressWarnings("unused")
@AutoService(Processor.class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.DYNAMIC)
public class MvpCompiler extends AbstractProcessor {
	public static final String DEFAULT_MOXY_REFLECTOR_PACKAGE = "com.omegar.mvp";
	private static final String MOXY_ANNOTATION_INJECT_VIEW_STATE = "com.omegar.mvp.InjectViewState";
	private static final String OPTION_MOXY_REFLECTOR_PACKAGE = "moxyReflectorPackage";
	private static final String OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES = "moxyRegisterReflectorPackages";
	private static final String OPTION_ENABLE_ISOLATING_PROCESSING = "moxyEnableIsolatingProcessing";

	private static Messager sMessager;
	private static Types sTypeUtils;
	private static Elements sElementUtils;
	private static Map<String, String> sOptions;

	private AnnotationInfo<TypeElement> mInjectViewStateAnnotationInfo;
	private AnnotationInfo<VariableElement> mInjectPresenterAnnotationInfo;

	public static Messager getMessager() {
		return sMessager;
	}

	public static Types getTypeUtils() {
		return sTypeUtils;
	}

	public static Elements getElementUtils() {
		return sElementUtils;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		sMessager = processingEnv.getMessager();
		sTypeUtils = processingEnv.getTypeUtils();
		sElementUtils = processingEnv.getElementUtils();
		sOptions = processingEnv.getOptions();

		mInjectViewStateAnnotationInfo = new AnnotationInfo<>(sElementUtils.getTypeElement(MOXY_ANNOTATION_INJECT_VIEW_STATE), AnnotationTarget.CLASS);
		mInjectPresenterAnnotationInfo = new AnnotationInfo<>(sElementUtils.getTypeElement(InjectPresenter.class.getCanonicalName()), AnnotationTarget.FIELD);
	}

	@Override
	public Set<String> getSupportedOptions() {
		return new HashSet<>(Arrays.asList(
				OPTION_MOXY_REFLECTOR_PACKAGE,
				OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES,
				OPTION_ENABLE_ISOLATING_PROCESSING,
				getCurrentIncrementalAnnotationProcessorType().getProcessorOption()
		));
	}

	private boolean isEnabledIsolatingProcessing() {
		String s = sOptions.get(OPTION_ENABLE_ISOLATING_PROCESSING);
		return Boolean.parseBoolean(s);
	}

	private IncrementalAnnotationProcessorType getCurrentIncrementalAnnotationProcessorType() {
		return isEnabledIsolatingProcessing()
				? IncrementalAnnotationProcessorType.ISOLATING
				: IncrementalAnnotationProcessorType.AGGREGATING;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> supportedAnnotationTypes = new HashSet<>();
		Collections.addAll(supportedAnnotationTypes,
				InjectPresenter.class.getCanonicalName(),
				MOXY_ANNOTATION_INJECT_VIEW_STATE,
				RegisterMoxyReflectorPackages.class.getCanonicalName());
		return supportedAnnotationTypes;
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
			return throwableProcess(roundEnv);
		} catch (RuntimeException e) {
			getMessager().printMessage(Diagnostic.Kind.OTHER, "Moxy compilation failed. Could you copy stack trace above and write us (or make issue on Github)?");
			e.printStackTrace();
		}

		return true;
	}

	private boolean throwableProcess(RoundEnvironment roundEnv) {


		String currentMoxyReflectorPackage = sOptions.getOrDefault(OPTION_MOXY_REFLECTOR_PACKAGE, DEFAULT_MOXY_REFLECTOR_PACKAGE);

		checkInjectors(roundEnv, new PresenterInjectorRules(mInjectPresenterAnnotationInfo, Modifier.PUBLIC, Modifier.DEFAULT));

		Publisher<TypeElement> presenterContainerElementPublisher = new Publisher<>();
		Publisher<TypeElement> presenterElementPublisher = new Publisher<>();
		Publisher<TypeElement> viewElementPublisher = new Publisher<>();
		Publisher<TypeElement> strategiesElementPublisher = new Publisher<>();
		Publisher<String> reflectorPackagesPublisher = new Publisher<>(getAdditionalMoxyReflectorPackages(roundEnv));

		Filer filer = processingEnv.getFiler();

		// presenterBinderPipeline
		new Pipeline.Builder<>(new ElementByAnnotationGenerator<>(roundEnv, mInjectPresenterAnnotationInfo))
				.addProcessor(new VariableToContainerElementProcessor())
				.uniqueFilter()
				.copyPublishTo(presenterContainerElementPublisher)
				.addProcessor(new ElementToTargetClassInfoProcessor())
				.addProcessor(new TargetClassInfoToPresenterBinderJavaFileProcessor())
				.buildPipeline(new JavaFileWriter(filer))
				.start();

		// viewStateProviderPipeline
		new Pipeline.Builder<>(new ElementByAnnotationGenerator<>(roundEnv, mInjectViewStateAnnotationInfo))
				.addProcessor(new ElementToPresenterInfoProcessor(viewElementPublisher))
				.addValidator(new NormalPresenterValidator())
				.copyTypeElementTo(presenterElementPublisher)
				.addProcessor(new PresenterInfoToViewStateProviderJavaFileProcessor().withCache())
				.buildPipeline(new JavaFileWriter(filer))
				.start();

		// viewStatePipeline
		new Pipeline.Builder<>(viewElementPublisher)
				.addProcessor(new ElementToViewInterfaceInfoProcessor(strategiesElementPublisher))
				.addProcessor(new ViewInterfaceInfoToViewStateJavaFileProcessor(currentMoxyReflectorPackage, reflectorPackagesPublisher))
				.buildPipeline(new JavaFileWriter(filer))
				.start();


		QuadPublisher<List<TypeElement>, List<TypeElement>, List<TypeElement>, List<String>> reflectorPublisher = presenterElementPublisher
				.collect()
				.quad(presenterContainerElementPublisher.collect(),
						strategiesElementPublisher.collect(),
						reflectorPackagesPublisher.collect());

		// moxyReflectorPipeline
		new Pipeline.Builder<>(reflectorPublisher)
				.addProcessor(new MoxyReflectorProcessor(currentMoxyReflectorPackage))
				.buildPipeline(new JavaFileWriter(filer))
				.start();


		return true;
	}

	private List<String> getAdditionalMoxyReflectorPackages(RoundEnvironment roundEnv) {
		List<String> result = new ArrayList<>();
		result.addAll(getOptionsAdditionalReflectorPackages());
		result.addAll(getRegisterAdditionalMoxyReflectorPackages(roundEnv));
		result.remove(DEFAULT_MOXY_REFLECTOR_PACKAGE);
		return result;
	}

	private List<String> getRegisterAdditionalMoxyReflectorPackages(RoundEnvironment roundEnv) {
		List<String> result = new ArrayList<>();

		for (Element element : roundEnv.getElementsAnnotatedWith(RegisterMoxyReflectorPackages.class)) {
			if (element.getKind() != ElementKind.CLASS) {
				getMessager().printMessage(Diagnostic.Kind.ERROR, element + " must be " + ElementKind.CLASS.name() + ", or not mark it as @" + RegisterMoxyReflectorPackages.class.getSimpleName());
			}

			String[] packages = element.getAnnotation(RegisterMoxyReflectorPackages.class).value();

			Collections.addAll(result, packages);
		}

		return result;
	}

	private List<String> getOptionsAdditionalReflectorPackages() {
		String moxyRegisterReflectorPackage = sOptions.get(OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES);

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
			getMessager().printMessage(Diagnostic.Kind.ERROR, errorStack);
		}
	}

}
