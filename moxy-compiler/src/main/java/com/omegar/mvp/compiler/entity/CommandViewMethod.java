package com.omegar.mvp.compiler.entity;

import com.omegar.mvp.compiler.Util;

import java.util.Objects;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Types;

/**
 * Date: 27-Jul-2017
 * Time: 12:58
 *
 * @author Evgeny Kursakov
 */
@SuppressWarnings("NewApi")
public class CommandViewMethod extends ViewMethod {

	private final String mUniqueSuffix;
	private final TypeElement mStrategy;
	private final String mTag;

	public CommandViewMethod(Types types, DeclaredType targetInterfaceElement,
							 ExecutableElement methodElement,
							 TypeElement strategy,
							 String tag,
							 String uniqueSuffix) {
		super(types, targetInterfaceElement, methodElement);
		mStrategy = strategy;
		mTag = tag;
		mUniqueSuffix = uniqueSuffix;
	}

	public TypeElement getStrategy() {
		return mStrategy;
	}

	public String getTag() {
		return mTag;
	}

	public String getUniqueSuffix() {
		return mUniqueSuffix;
	}

	public String getCommandClassName() {
		return Util.capitalizeString(getName()) + mUniqueSuffix + "Command";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;
		CommandViewMethod that = (CommandViewMethod) o;
		return Objects.equals(mUniqueSuffix, that.mUniqueSuffix) &&
				Objects.equals(mStrategy, that.mStrategy) &&
				Objects.equals(mTag, that.mTag);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), mUniqueSuffix, mStrategy, mTag);
	}
}
