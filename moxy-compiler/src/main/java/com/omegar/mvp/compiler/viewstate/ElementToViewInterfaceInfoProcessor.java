package com.omegar.mvp.compiler.viewstate;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.compiler.entity.ReturnViewMethod;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.entity.CommandViewMethod;
import com.omegar.mvp.compiler.entity.ViewMethod;
import com.omegar.mvp.compiler.pipeline.ElementProcessor;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.compiler.pipeline.Publisher;
import com.omegar.mvp.compiler.pipeline.PipelineContext;
import com.omegar.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.omegar.mvp.viewstate.strategy.StrategyType;
import com.omegar.mvp.viewstate.strategy.StateStrategyType;
import com.squareup.javapoet.ClassName;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


import static com.omegar.mvp.compiler.Util.MVP_VIEW_CLASS_NAME;
import static com.omegar.mvp.compiler.Util.asElement;

/**
 * Date: 27-Jul-2017
 * Time: 13:09
 *
 * @author Evgeny Kursakov
 */
@SuppressWarnings("NewApi")
public class ElementToViewInterfaceInfoProcessor extends ElementProcessor<TypeElement, ViewInterfaceInfo> {
    private static final String STATE_STRATEGY_TYPE_ANNOTATION = StateStrategyType.class.getName();
    private static final String GETTER_PREFIX = "get";
    private static final String SETTER_PREFIX = "set";


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
        ViewInterfaceInfo info = generateInfo(element);
        if (info != null) {
            context.next(info);
        }
    }

    @Nullable
    private ViewInterfaceInfo generateInfo(TypeElement element) {
        if (ClassName.get(element).equals(MVP_VIEW_CLASS_NAME)) return null;

        // Get methods for input class
        List<ViewMethod> methods = getMethods(element);

        // get super interface
        TypeElement superInterfaceType = getSuperInterfaceTypeElement(element);

        return new ViewInterfaceInfo(superInterfaceType, element, methods);
    }

    @Nullable
    private TypeElement getSuperInterfaceTypeElement(TypeElement element) {
        for (TypeMirror typeMirror : element.getInterfaces()) {
            final TypeElement interfaceElement = asElement(typeMirror);

            if (interfaceElement != null && mTypes.isAssignable(interfaceElement.asType(), mMvpViewTypeMirror)) {
                return interfaceElement;
            }
        }
        return null;
    }

    private List<ViewMethod> getMethods(TypeElement typeElement) {
        Map<String, Integer> methodsCounter = new HashMap<>();

        Map<Boolean, List<ExecutableElement>> elementMap = typeElement.getEnclosedElements()
                .stream()
                .map(this::getExecutableElement)
                .filter(Objects::nonNull)
                .collect(Collectors.partitioningBy(element -> element.getReturnType().getKind() != TypeKind.VOID));


        List<ExecutableElement> voidElements = elementMap.get(false);

        Map<ExecutableElement, ExecutableElement> setterGetterMap = new HashMap<>();

        Stream<CommandViewMethod> voidStream = voidElements
                .stream()
                .map(methodElement -> {
                    AnnotationMirror annotation = Util.getAnnotation(methodElement, STATE_STRATEGY_TYPE_ANNOTATION);

                    TypeElement strategyClass = getStrategyClass(typeElement, methodElement, annotation, setterGetterMap);

                    // publish strategy
                    mUsedStrategiesPublisher.next(strategyClass);

                    String methodTag = getMethodTag(annotation, methodElement);

                    // Allow methods be with same names
                    String uniqueSuffix = getUniqueSuffix(methodsCounter, methodElement);

                    boolean singleInstance = Util.getAnnotationValueAsBoolean(annotation, "singleInstance");

                    return new CommandViewMethod(mTypes,
                            (DeclaredType) typeElement.asType(),
                            methodElement,
                            strategyClass,
                            methodTag,
                            uniqueSuffix,
                            singleInstance
                    );

                });

        Stream<ReturnViewMethod> returnStream = elementMap.get(true)
                .stream()
                .map(getterElement -> {
                    ExecutableElement setterElement = getSetterElement(getterElement, voidElements);
                    if (setterElement == null) {
                        String message = String.format("You are trying generate ViewState for %s. " +
                                        "But %s contains non-void method \"%s\" that return type is %s. " +
                                        "See more here: https://github.com/Arello-Mobile/Moxy/issues/2",
                                typeElement.getSimpleName(),
                                typeElement.getSimpleName(),
                                getterElement.getSimpleName(),
                                getterElement.getReturnType()
                        );
                        throw new IllegalArgumentException(message);
                    } else {
                        setterGetterMap.put(setterElement, getterElement);
                    }
                    return new ReturnViewMethod(mTypes, (DeclaredType) typeElement.asType(), getterElement, setterElement);
                });

        return Stream.concat(returnStream, voidStream)
                .collect(Collectors.toList());
    }

    private ExecutableElement getSetterElement(ExecutableElement getterElement,
                                               List<ExecutableElement> setterElements) {
        if (!getterElement.getParameters().isEmpty()) return null;
        String methodName = getterElement.getSimpleName().toString();

        if (!Util.startWith(methodName, GETTER_PREFIX)) return null;

        String setterMethod = SETTER_PREFIX + methodName.substring(GETTER_PREFIX.length());

        for (ExecutableElement setterElement : setterElements) {
            if (setterElement.getSimpleName().contentEquals(setterMethod)) {
                List<? extends VariableElement> parameters = setterElement.getParameters();
                if (parameters.size() == 1) {
                    TypeMirror paramTypeMirror = parameters.get(0).asType();
                    if (paramTypeMirror instanceof DeclaredType) {
                        if (((DeclaredType) paramTypeMirror).asElement().equals(Util.asElement(getterElement.getReturnType()))) {
                            return setterElement;
                        }
                    } else if (parameters.get(0).asType().equals(getterElement.getReturnType())) {
                        return setterElement;
                    }
                }
            }
        }
        return null;
    }

    private String getUniqueSuffix(Map<String, Integer> methodsCounter, ExecutableElement methodElement) {
        String methodName = ViewMethod.getMethodName(methodElement);
        Integer counter = methodsCounter.get(methodName);
        String uniqueSuffix = "";

        if (counter != null && counter > 0) {
            uniqueSuffix = String.valueOf(counter);
        } else {
            counter = 0;
        }

        methodsCounter.put(methodName, counter + 1);
        return uniqueSuffix;
    }

    @Nullable
    private ExecutableElement getExecutableElement(Element element) {
        // ignore all but non-static methods
        if (element.getKind() != ElementKind.METHOD || element.getModifiers().contains(Modifier.STATIC)) {
            return null;
        }
        return (ExecutableElement) element;
    }

    @Nonnull
    private TypeElement getStrategyClass(TypeElement typeElement,
                                         ExecutableElement methodElement,
                                         AnnotationMirror annotation,
                                         Map<ExecutableElement, ExecutableElement> setterGetterMap) {
        StrategyType type = Util.getAnnotationValueAsStrategyType(annotation, "value");

        if (setterGetterMap.get(methodElement) != null) {
            if (type != null) {
                mMessager.printMessage(Diagnostic.Kind.WARNING,
                        String.format("\n Remove strategy for method %s\n\n", methodElement.getSimpleName()),
                        typeElement);
            }
            return mElements.getTypeElement(AddToEndSingleStrategy.class.getCanonicalName());
        }

        if (type == StrategyType.CUSTOM || type == null) {
            // get strategy from annotation
            TypeMirror strategyClassFromAnnotation = Util.getAnnotationValueAsTypeMirror(annotation, "custom");
            if (strategyClassFromAnnotation != null) {
                return (TypeElement) ((DeclaredType) strategyClassFromAnnotation).asElement();
            }
        } else {
            return mElements.getTypeElement(type.getStrategyClass().getCanonicalName());
        }

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
        throw new IllegalArgumentException(message);
    }

    private String getMethodTag(AnnotationMirror annotation, ExecutableElement methodElement) {
        // get tag from annotation
        String tagFromAnnotation = Util.getAnnotationValueAsString(annotation, "tag");

        if (tagFromAnnotation != null) {
            return tagFromAnnotation;
        } else {
            return methodElement.getSimpleName().toString();
        }
    }

    @Override
    protected void finish(PipelineContext<ViewInterfaceInfo> nextContext) {
        mUsedStrategiesPublisher.finish();
        super.finish(nextContext);
    }

}
