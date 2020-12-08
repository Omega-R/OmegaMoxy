package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.entity.ViewMethod;
import com.omegar.mvp.compiler.pipeline.ElementProcessor;
import com.omegar.mvp.compiler.MvpCompiler;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.compiler.pipeline.Publisher;
import com.omegar.mvp.compiler.pipeline.PipelineContext;
import com.omegar.mvp.viewstate.strategy.StrategyType;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;
import com.squareup.javapoet.ParameterSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
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
public class ElementToViewInterfaceInfoProcessor extends ElementProcessor<TypeElement, com.omegar.mvp.compiler.entity.ViewInterfaceInfo> {
	private static final String STATE_STRATEGY_TYPE_ANNOTATION = StateStrategyType.class.getName();

	private final Publisher<TypeElement> mUsedStrategiesPublisher;

	private TypeElement mViewInterfaceElement;
	private String mViewInterfaceName;

	public ElementToViewInterfaceInfoProcessor(Publisher<TypeElement> usedStrategiesPublisher) {
		mUsedStrategiesPublisher = usedStrategiesPublisher;
	}

	@Override
	public void process(TypeElement element, PipelineContext<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> context) {
		Collection<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> list = generateInfos(element);
		for (com.omegar.mvp.compiler.entity.ViewInterfaceInfo info: list) {
			context.next(info);
		}
	}

	private void fillWithNotInheredMethods(List<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> list) {
		for (com.omegar.mvp.compiler.entity.ViewInterfaceInfo info : list) {
			TypeElement element = info.getTypeElement();
			List<com.omegar.mvp.compiler.entity.ViewMethod> infoMethods = info.getMethods();

			if (info.getSuperTypeMvpElements().size() > 1) {
				List<com.omegar.mvp.compiler.entity.ViewMethod> inheredMethods = getInheredMethods(info);
				for (com.omegar.mvp.compiler.entity.ViewMethod method : getNotInheredMethods(info, list)) {
					if (!inheredMethods.contains(method)) {
						infoMethods.add(new com.omegar.mvp.compiler.entity.ViewMethod((DeclaredType) element.asType(), method));
					}
				}
			}
		}
	}

	private List<com.omegar.mvp.compiler.entity.ViewMethod> getInheredMethods(com.omegar.mvp.compiler.entity.ViewInterfaceInfo info) {
		List<com.omegar.mvp.compiler.entity.ViewMethod> methods = new ArrayList<>(info.getMethods());

		com.omegar.mvp.compiler.entity.ViewInterfaceInfo superInterfaceInfo = info.getSuperInterfaceInfo();
		if (superInterfaceInfo != null) methods.addAll(getInheredMethods(superInterfaceInfo));

		return methods;
	}

	private Set<com.omegar.mvp.compiler.entity.ViewMethod> getNotInheredMethods(com.omegar.mvp.compiler.entity.ViewInterfaceInfo info, List<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> infoList) {
		List<TypeElement> elements = info.getSuperTypeMvpElements();
		if (elements.size() <= 1) return Collections.emptySet();

		assert info.getSuperInterfaceInfo() != null;
		TypeElement superClassElement = info.getSuperInterfaceInfo().getTypeElement();

		Set<com.omegar.mvp.compiler.entity.ViewMethod> methodSet = new LinkedHashSet<>();
		for (TypeElement element : elements) {
			if (!element.equals(superClassElement)) {
				com.omegar.mvp.compiler.entity.ViewInterfaceInfo infoByType = getViewInterfaceInfoByTypeElement(infoList, element);
				if (infoByType != null) {
					methodSet.addAll(getInheredMethods(infoByType));
					methodSet.addAll(getNotInheredMethods(infoByType, infoList));
				}
			}
		}
		return methodSet;
	}

	private com.omegar.mvp.compiler.entity.ViewInterfaceInfo getViewInterfaceInfoByTypeElement(List<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> list, TypeElement element) {
		for (com.omegar.mvp.compiler.entity.ViewInterfaceInfo info : list) {
			if (info.getTypeElement().equals(element)) return info;
		}
		return null;
	}

	private Set<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> generateInfos(TypeElement element) {
		Set<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> interfaceInfos = new LinkedHashSet<>();
		this.mViewInterfaceElement = element;
		mViewInterfaceName = element.getSimpleName().toString();

		List<com.omegar.mvp.compiler.entity.ViewMethod> methods = new ArrayList<>();

		// Get methods for input class
		getMethods(element, new ArrayList<>(), methods);

        // Add methods from super interfaces
		com.omegar.mvp.compiler.entity.ViewInterfaceInfo superInterfaceInfo = null;
		for (TypeMirror typeMirror : element.getInterfaces()) {
			final TypeElement interfaceElement = asElement(typeMirror);
			if (isMvpElement(interfaceElement)) {
				Set<com.omegar.mvp.compiler.entity.ViewInterfaceInfo> parentInfos = generateInfos(interfaceElement);
				if (superInterfaceInfo == null) {
					superInterfaceInfo = Util.lastOrNull(parentInfos);
				}
            }
		}

		// Allow methods be with same names
		Map<String, Integer> methodsCounter = new HashMap<>();
		for (com.omegar.mvp.compiler.entity.ViewMethod method : methods) {
			Integer counter = methodsCounter.get(method.getName());

			if (counter != null && counter > 0) {
				method.setUniqueSuffix(String.valueOf(counter));
			} else {
				counter = 0;
			}

			counter++;
			methodsCounter.put(method.getName(), counter);
		}

		com.omegar.mvp.compiler.entity.ViewInterfaceInfo info = new com.omegar.mvp.compiler.entity.ViewInterfaceInfo(superInterfaceInfo, element, methods);
		if (!info.getName().equals(MVP_VIEW_CLASS_NAME)) interfaceInfos.add(info);

		return interfaceInfos;
	}

	private void getMethods(TypeElement typeElement,
							List<com.omegar.mvp.compiler.entity.ViewMethod> rootMethods,
							List<com.omegar.mvp.compiler.entity.ViewMethod> superinterfacesMethods) {


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
				MvpCompiler.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
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
				strategyClass = MvpCompiler.getElementUtils().getTypeElement(type.getStrategyClass().getName());
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
				MvpCompiler.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
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

			final com.omegar.mvp.compiler.entity.ViewMethod method = new com.omegar.mvp.compiler.entity.ViewMethod(
					(DeclaredType) mViewInterfaceElement.asType(), methodElement, strategyClass, methodTag
			);

			if (rootMethods.contains(method)) {
				continue;
			}

			if (superinterfacesMethods.contains(method)) {
				checkStrategyAndTagEquals(method, superinterfacesMethods.get(superinterfacesMethods.indexOf(method)));
				continue;
			}

			superinterfacesMethods.add(method);
		}
	}

	private void checkStrategyAndTagEquals(com.omegar.mvp.compiler.entity.ViewMethod method, ViewMethod existingMethod) {
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

			String parts = differentParts.stream().collect(Collectors.joining(" and "));

			throw new IllegalStateException("Both " + existingMethod.getEnclosedClassName() +
					" and " + method.getEnclosedClassName() +
					" has method " + method.getName() + "(" + arguments + ")" +
					" with different " + parts + "." +
					" Override this method in " + mViewInterfaceName + " or make " + parts + " equals");
		}
	}

	@Override
	protected void finish(PipelineContext<ViewInterfaceInfo> nextContext) {
		mUsedStrategiesPublisher.finish();
		super.finish(nextContext);
	}
}