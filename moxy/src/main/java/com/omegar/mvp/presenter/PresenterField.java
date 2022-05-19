package com.omegar.mvp.presenter;

import com.omegar.mvp.MvpPresenter;

/**
 * Date: 18-Dec-15
 * Time: 17:50
 *
 * @author Alexander Blinov
 * @author Yuri Shmakov
 */
@SuppressWarnings("rawtypes")
public abstract class PresenterField<PresentersContainer> {
	protected final String tag;
	protected final PresenterType presenterType;
	protected final String presenterId;

	protected PresenterField(String tag, PresenterType presenterType, String presenterId) {
		this.tag = tag;
		this.presenterType = presenterType;
		this.presenterId = presenterId;
	}

	public abstract void bind(PresentersContainer container, MvpPresenter presenter);

	// Delegated may be used from generated code if user plane to generate tag at runtime
	@SuppressWarnings("unused")
	public String getTag(PresentersContainer delegated) {
		return tag;
	}

	public PresenterType getPresenterType() {
		return presenterType;
	}

	public String getPresenterId() {
		return presenterId;
	}

	public abstract MvpPresenter<?> providePresenter(PresentersContainer delegated);
}
