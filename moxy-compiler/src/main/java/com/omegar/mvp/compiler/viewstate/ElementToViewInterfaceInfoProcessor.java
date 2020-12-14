package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.entity.ViewMethod;
import com.omegar.mvp.compiler.pipeline.ElementProcessor;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.compiler.pipeline.Publisher;
import com.omegar.mvp.compiler.pipeline.PipelineContext;
import com.omegar.mvp.viewstate.strategy.StrategyType;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import static com.omegar.mvp.compiler.Util.MVP_VIEW_CLASS_NAME;
import static com.omegar.mvp.compiler.Util.asElement;
import static com.omegar.mvp.compiler.Util.isMvpElement;

/**
 * Date: 27-Jul-2017
 * Time: 13:09
 *
 * @author Evgeny Kursakov
 */
@SuppressWarnings("NewApi")
public class ElementToViewInterfaceInfoProcessor extends ElementProcessor<TypeElement, ViewInterfaceInfo> {
	private static final String STATE_STRATEGY_TYPE_ANNOTATION = StateStrategyType.class.getName();

	private final Elements mElements;
	private final Types mTypes;
	private final Messager mMessager;
	private final Publisher<TypeElement> mUsedStrategiesPublisher;
	private final TypeMirror mMvpViewTypeMirror;


	public ElementToViewInterfaceInfoProcessor(Elements elements, Types types, Messager messager, Publisher<TypeElement> usedStrategiesPublisher) {
		mElements = elements;
		mTypes = types;
		mMessager = messager;
		mUsedStrategiesPublisher = usedStrategiesPublisher;
		mMvpViewTypeMirror = elements.getTypeElement(MvpView.class.getCanonicalName()).asType();
	}

	@Override
	public void process(TypeElement element, PipelineContext<ViewInterfaceInfo> context) {
		Collection<ViewInterfaceInfo> list = generateInfos(element);

		for (ViewInterfaceInfo info: list) {
			context.next(info);
		}
	}

	private Set<ViewInterfaceInfo> generateInfos(TypeElement element) {
		Set<ViewInterfaceInfo> interfaceInfos = new LinkedHashSet<>();

		List<ViewMethod> methods = new ArrayList<>();

		// Get methods for input class
		getMethods(element, new ArrayList<>(), methods, element);

        // Add methods from super interfaces
		ViewInterfaceInfo superInterfaceInfo = null;
		for (TypeMirror typeMirror : element.getInterfaces()) {
			final TypeElement interfaceElement = asElement(typeMirror);

			if (interfaceElement != null && mTypes.isAssignable(interfaceElement.asType(), mMvpViewTypeMirror)) {
				Set<ViewInterfaceInfo> parentInfos = generateInfos(interfaceElement);
				if (superInterfaceInfo == null) {
					superInterfaceInfo = Util.lastOrNull(parentInfos);
				}
            }
		}

		// Allow methods be with same names
		Map<String, Integer> methodsCounter = new HashMap<>();
		for (ViewMethod method : methods) {
			Integer counter = methodsCounter.get(method.getName());

			if (counter != null && counter > 0) {
				method.setUniqueSuffix(String.valueOf(counter));
			} else {
				counter = 0;
			}

			counter++;
			methodsCounter.put(method.getName(), counter);
		}

		ViewInterfaceInfo info = new ViewInterfaceInfo(superInterfaceInfo, element, methods);
		if (!info.getName().equals(MVP_VIEW_CLASS_NAME)) interfaceInfos.add(info);

		return interfaceInfos;
	}

	private void getMethods(TypeElement typeElement,
							List<ViewMethod> rootMethods,
							List<ViewMethod> superinterfacesMethods,
							TypeElement viewInterfaceElement) {


		for (Element element : typeElement.getEnclosedElements()) {
			// ignore all but non-static methods
			if (element.getKind() != ElementKind.METHOD || element.getModifiers().contains(Modifier.STATIC)) {
				continue;
			}

			final ExecutableElement methodElement = (ExecutableElement) element;

			if (methodElement.getReturnType().getKind() != TypeKind.VOID) {
				String message = String.format("You are trying generate ViewState for %s. " +
								"But %s contains non-void method \"%s\" that return type is %s. " +
								"See more here: https://github.com/Arello-Mobile/Moxy/issues/2",
						typeElement.getSimpleName(),
						typeElement.getSimpleName(),
						methodElement.getSimpleName(),
						methodElement.getReturnType()
				);
				mMessager.printMessage(Diagnostic.Kind.ERROR, message);
			}

			AnnotationMirror annotation = Util.getAnnotation(methodElement, STATE_STRATEGY_TYPE_ANNOTATION);

			StrategyType type = Util.getAnnotationValueAsStrategyType(annotation, "value");

			TypeElement strategyClass = null;

			if (type == StrategyType.CUSTOM || type == null) {
				// get strategy from annotation
				TypeMirror strategyClassFromAnnotation = Util.getAnnotationValueAsTypeMirror(annotation, "custom");
				if (strategyClassFromAnnotation != null) {
					strategyClass = (TypeElement) ((DeclaredType) strategyClassFromAnnotation).asElement();
				}
			} else {
				strategyClass = mElements.getTypeElement(type.getStrategyClass().getCanonicalName());
			}

			if (strategyClass == null) {
				String message = String.format("\nYou are trying generate ViewState for %s. " +
								"But %s interface and \"%s\" method don't provide Strategy type. " +
								"Please annotate your %s interface or method with Strategy." + "\n\n" +
								"For example:\n@StateStrategyType(ADD_TO_END_SINGLE)" + "\n" + "fun %s()\n\n",
						typeElement.getSimpleName(),
						typeElement.getSimpleName(),
						methodElement.getSimpleName(),
						typeElement.getSimpleName(),
						methodElement.getSimpleName()
				);
				mMessager.printMessage(Diagnostic.Kind.ERROR, message);
				return;
			}

			// get tag from annotation
			String tagFromAnnotation = Util.getAnnotationValueAsString(annotation, "tag");

			String methodTag;
			if (tagFromAnnotation != null) {
				methodTag = tagFromAnnotation;
			} else {
				methodTag = methodElement.getSimpleName().toString();
			}

			// add strategy to list
			mUsedStrategiesPublisher.next(strategyClass);

			final ViewMethod method = new ViewMethod(mTypes,
					(DeclaredType) viewInterfaceElement.asType(), methodElement, strategyClass, methodTag
			);

			if (rootMethods.contains(method)) {
				continue;
			}

			if (superinterfacesMethods.contains(method)) {
				checkStrategyAndTagEquals(method, superinterfacesMethods.get(superinterfacesMethods.indexOf(method)), viewInterfaceElement);
				continue;
			}

			superinterfacesMethods.add(method);
		}
	}

	private void checkStrategyAndTagEquals(ViewMethod method, ViewMethod existingMethod, TypeElement viewInterfaceName) {
		List<String> differentParts = new ArrayList<>();
		if (!existingMethod.getStrategy().equals(method.getStrategy())) {
			differentParts.add("strategies");
		}
		if (!existingMethod.getTag().equals(method.getTag())) {
			differentParts.add("tags");
		}

		if (!differentParts.isEmpty()) {
			String arguments = method.getParameterSpecs().stream()
					.map(ParameterSpec::toString)
					.collect(Collectors.joining(", "));

			String parts = String.join(" and ", differentParts);

			throw new IllegalStateException("Both " + existingMethod.getEnclosedClassName() +
					" and " + method.getEnclosedClassName() +
					" has method " + method.getName() + "(" + arguments + ")" +
					" with different " + parts + "." +
					" Override this method in " + viewInterfaceName.getSimpleName().toString() + " or make " + parts + " equals");
		}
	}

	@Override
	protected void finish(PipelineContext<ViewInterfaceInfo> nextContext) {
		mUsedStrategiesPublisher.finish();
		super.finish(nextContext);
	}
}
