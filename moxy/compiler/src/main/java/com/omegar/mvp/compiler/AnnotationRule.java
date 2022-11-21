package com.omegar.mvp.compiler;

import com.omegar.mvp.compiler.entity.AnnotationInfo;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * Date: 17-Feb-16
 * Time: 16:57
 *
 * @author esorokin
 */
public abstract class AnnotationRule {
	protected final Set<Modifier> mValidModifiers;
	protected final AnnotationInfo<?> mAnnotationInfo;
	protected StringBuilder mErrorBuilder;

	public AnnotationRule(AnnotationInfo<?> annotationInfo, Modifier... validModifiers) {
		mAnnotationInfo = annotationInfo;
		if (validModifiers == null || validModifiers.length == 0) {
			throw new RuntimeException("Valid modifiers cant be empty or null.");
		}

		mValidModifiers = new HashSet<>(Arrays.asList(validModifiers));
		mErrorBuilder = new StringBuilder();
	}

	/**
	 * Method describe rules for using Annotation.
	 *
	 * @param AnnotatedField Checking annotated field.
	 */
	public abstract void checkAnnotation(Element AnnotatedField);

	public String getErrorStack() {
		return mErrorBuilder.toString();
	}

	protected String validModifiersToString() {
		if (mValidModifiers.size() > 1) {
			StringBuilder result = new StringBuilder("one of [");
			boolean addSeparator = false;
			for (Modifier validModifier : mValidModifiers) {
				if (addSeparator) {
					result.append(", ");
				}
				addSeparator = true;
				result.append(validModifier.toString());
			}
			result.append("]");
			return result.toString();
		} else {
			return mValidModifiers.iterator().next() + ".";
		}
	}
}
