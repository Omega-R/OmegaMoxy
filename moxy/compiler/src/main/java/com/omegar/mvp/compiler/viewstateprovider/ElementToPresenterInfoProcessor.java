package com.omegar.mvp.compiler.viewstateprovider;

import static com.omegar.mvp.compiler.Util.fillGenerics;

import com.omegar.mvp.MvpPresenter;
import com.omegar.mvp.compiler.Util;
import com.omegar.mvp.compiler.entity.PresenterInfo;
import com.omegar.mvp.compiler.entity.ViewInterfaceInfo;
import com.omegar.mvp.compiler.pipeline.ElementProcessor;
import com.omegar.mvp.compiler.pipeline.PipelineContext;
import com.omegar.mvp.compiler.pipeline.Publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class ElementToPresenterInfoProcessor extends ElementProcessor<TypeElement, PresenterInfo> {
    private static final String MVP_PRESENTER_CLASS = MvpPresenter.class.getCanonicalName();

    private final Elements mElements;
    private final Publisher<TypeElement> mUsedViewsPublisher;

    public ElementToPresenterInfoProcessor(Elements elements, Publisher<TypeElement> usedViewsPublisher) {
        mElements = elements;
        mUsedViewsPublisher = usedViewsPublisher;
    }

    @Override
    public PresenterInfo process(TypeElement element) {
        TypeElement viewClassName = getViewClassName(element);

        String viewFullName = ViewInterfaceInfo.getViewFullName(mElements, viewClassName);

        return new PresenterInfo(
                element,
                ViewInterfaceInfo.getViewStateFullName(viewFullName),
                viewClassName
        );
    }

    private TypeElement getViewClassName(TypeElement typeElement) {
        String view = getViewClassFromGeneric(typeElement);

        // Remove generic from view class name
        String viewWithoutGeneric = Util.substringBefore(view, '<');

        TypeElement viewTypeElement = mElements.getTypeElement(viewWithoutGeneric);

        if (viewTypeElement == null) {
            throw new IllegalArgumentException("View \"" + view + "\" for " + typeElement + " cannot be found. \n " + view);
        }

        mUsedViewsPublisher.next(viewTypeElement);

        return viewTypeElement;
    }


    private String getViewClassFromGeneric(TypeElement typeElement) {
        TypeMirror superclass = typeElement.asType();

        Map<String, String> parentTypes = Collections.emptyMap();

        while (superclass.getKind() != TypeKind.NONE) {
            TypeElement superclassElement = (TypeElement) ((DeclaredType) superclass).asElement();

            final List<? extends TypeMirror> typeArguments = ((DeclaredType) superclass).getTypeArguments();
            final List<? extends TypeParameterElement> typeParameters = superclassElement.getTypeParameters();

            if (typeArguments.size() > typeParameters.size()) {
                throw new IllegalArgumentException("Code generation for interface " + typeElement.getSimpleName() + " failed. Simplify your generics. (" + typeArguments + " vs " + typeParameters + ")");
            }

            Map<String, String> types = new HashMap<>();
            for (int i = 0; i < typeArguments.size(); i++) {
                types.put(typeParameters.get(i).toString(), fillGenerics(parentTypes, typeArguments.get(i)));
            }

            if (superclassElement.toString().equals(MVP_PRESENTER_CLASS)) {
                // MvpPresenter is typed only on View class
                return fillGenerics(parentTypes, typeArguments);
            }

            parentTypes = types;

            superclass = superclassElement.getSuperclass();
        }

        return "";
    }

    @Override
    protected void finish(PipelineContext<PresenterInfo> nextContext) {
        mUsedViewsPublisher.finish();
        super.finish(nextContext);
    }
}
