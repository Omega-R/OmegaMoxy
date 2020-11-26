package com.omegar.mvp.compiler;

import com.google.auto.service.AutoService;
import com.omegar.mvp.RegisterMoxyReflectorPackages;
import com.omegar.mvp.compiler.presenterbinder.InjectPresenterProcessor;
import com.omegar.mvp.compiler.presenterbinder.PresenterBinderClassGenerator;
import com.omegar.mvp.compiler.reflector.MoxyReflectorGenerator;
import com.omegar.mvp.compiler.viewstate.ViewInterfaceProcessor;
import com.omegar.mvp.compiler.viewstate.ViewStateClassGenerator;
import com.omegar.mvp.compiler.viewstateprovider.InjectViewStateProcessor;
import com.omegar.mvp.compiler.viewstateprovider.ViewStateProviderClassGenerator;
import com.omegar.mvp.presenter.InjectPresenter;
import com.squareup.javapoet.JavaFile;

import net.ltgt.gradle.incap.IncrementalAnnotationProcessor;
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
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
	public static final String MOXY_REFLECTOR_DEFAULT_PACKAGE = "com.omegar.mvp";
	private static final String MOXY_ANNOTATION_INJECT_VIEW_STATE = "com.omegar.mvp.InjectViewState";
	private static final String OPTION_MOXY_REFLECTOR_PACKAGE = "moxyReflectorPackage";
	private static final String OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES = "moxyRegisterReflectorPackages";
	private static final String OPTION_ENABLE_ISOLATING_PROCESSING = "moxyEnableIsolatingProcessing";

	private static Messager sMessager;
	private static Types sTypeUtils;
	private static Elements sElementUtils;
	private static Map<String, String> sOptions;
	private static final List<Element> sUsedElements = new ArrayList<>();

	public static Messager getMessager() {
		return sMessager;
	}

	public static Types getTypeUtils() {
		return sTypeUtils;
	}

	public static Elements getElementUtils() {
		return sElementUtils;
	}

	public static List<Element> getUsedElements() {
		return sUsedElements;
	}

	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);

		sMessager = processingEnv.getMessager();
		sTypeUtils = processingEnv.getTypeUtils();
		sElementUtils = processingEnv.getElementUtils();
		sOptions = processingEnv.getOptions();
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
		sUsedElements.clear();

		String moxyReflectorPackage = sOptions.get(OPTION_MOXY_REFLECTOR_PACKAGE);
		if (moxyReflectorPackage == null) {
			moxyReflectorPackage = MOXY_REFLECTOR_DEFAULT_PACKAGE;
		}

		checkInjectors(roundEnv, InjectPresenter.class, new PresenterInjectorRules(ElementKind.FIELD, Modifier.PUBLIC, Modifier.DEFAULT));

		InjectViewStateProcessor injectViewStateProcessor = new InjectViewStateProcessor();
		ViewStateProviderClassGenerator viewStateProviderClassGenerator = new ViewStateProviderClassGenerator();

		InjectPresenterProcessor injectPresenterProcessor = new InjectPresenterProcessor();
		PresenterBinderClassGenerator presenterBinderClassGenerator = new PresenterBinderClassGenerator();

		ViewInterfaceProcessor viewInterfaceProcessor = new ViewInterfaceProcessor();
		ViewStateClassGenerator viewStateClassGenerator = new ViewStateClassGenerator(moxyReflectorPackage);

		processInjectors(
				roundEnv,
				getElementUtils().getTypeElement(MOXY_ANNOTATION_INJECT_VIEW_STATE),
				ElementKind.CLASS,
				injectViewStateProcessor,
				viewStateProviderClassGenerator
		);
		processInjectors(roundEnv,
				getElementUtils().getTypeElement(InjectPresenter.class.getCanonicalName()),
				ElementKind.FIELD,
				injectPresenterProcessor, presenterBinderClassGenerator);

		generateCode(injectViewStateProcessor.getUsedViews(), ElementKind.INTERFACE,
				viewInterfaceProcessor, viewStateClassGenerator);

		String moxyRegisterReflectorPackage = sOptions.get(OPTION_MOXY_REGISTER_REFLECTOR_PACKAGES);

		List<String> additionalMoxyReflectorPackages = new ArrayList<>();

		if (moxyRegisterReflectorPackage != null) {
			String[] strings = moxyRegisterReflectorPackage.split(",");
			additionalMoxyReflectorPackages.addAll(Arrays.asList(strings));
		}

		additionalMoxyReflectorPackages.addAll(getAdditionalMoxyReflectorPackages(roundEnv));
		additionalMoxyReflectorPackages.addAll(viewStateClassGenerator.getReflectorPackages());
		additionalMoxyReflectorPackages.remove(MOXY_REFLECTOR_DEFAULT_PACKAGE);

		JavaFile moxyReflector = MoxyReflectorGenerator.generate(
				moxyReflectorPackage,
				injectViewStateProcessor.getPresenterClassNames(),
				injectPresenterProcessor.getPresentersContainers(),
				viewInterfaceProcessor.getUsedStrategies(),
				additionalMoxyReflectorPackages
		);

		createSourceFile(moxyReflector);

		return true;
	}

	private List<String> getAdditionalMoxyReflectorPackages(RoundEnvironment roundEnv) {
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


	private void checkInjectors(final RoundEnvironment roundEnv, Class<? extends Annotation> clazz, AnnotationRule annotationRule) {
		for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(clazz)) {
			annotationRule.checkAnnotation(annotatedElement);
		}

		String errorStack = annotationRule.getErrorStack();
		if (errorStack != null && errorStack.length() > 0) {
			getMessager().printMessage(Diagnostic.Kind.ERROR, errorStack);
		}
	}

	private <E extends Element, R> void processInjectors(RoundEnvironment roundEnv,
	                                                     TypeElement annotationClass,
	                                                     ElementKind kind,
	                                                     ElementProcessor<E, R> processor,
	                                                     JavaFilesGenerator<R> classGenerator) {
		for (Element element : roundEnv.getElementsAnnotatedWith(annotationClass)) {
			if (element.getKind() != kind) {
				getMessager().printMessage(Diagnostic.Kind.ERROR,
						element + " must be " + kind.name() + ", or not mark it as @" + annotationClass.getSimpleName());
			}
			sUsedElements.add(element);
			generateCode(element, kind, processor, classGenerator);
		}
	}

    private <E extends Element, R> void generateCode(Set<TypeElement> elementSet,
                                                     ElementKind kind,
                                                     ElementProcessor<E, List<R>> processor,
                                                     JavaFilesGenerator<List<R>> classGenerator) {
        Set<JavaFile> fileSet = new HashSet<>();
        for (Element element : elementSet) {
            List<R> list = generateCode(element, kind, processor);
            if (list != null) fileSet.addAll(classGenerator.generate(list));
        }
        for (JavaFile file : fileSet) {
            createSourceFile(file);
        }
    }

	private <E extends Element, R> void generateCode(Element element,
													 ElementKind kind,
													 ElementProcessor<E, R> processor,
													 JavaFilesGenerator<R> classGenerator) {
		R result = generateCode(element, kind, processor);
		if (result == null) return;
		for (JavaFile file : classGenerator.generate(result)) {
			createSourceFile(file);
		}
	}

	private <E extends Element, R> R generateCode(Element element,
                                                  ElementKind kind,
                                                  ElementProcessor<E, R> processor) {
		if (element.getKind() != kind) {
			getMessager().printMessage(Diagnostic.Kind.ERROR, element + " must be " + kind.name());
		}
		//noinspection unchecked
		return processor.process((E) element);
	}

	private void createSourceFile(JavaFile file) {
		try {
			file.writeTo(processingEnv.getFiler());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
