package com.omegar.mvp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.omegar.mvp.presenter.PresenterField;
import com.omegar.mvp.presenter.PresenterType;
import com.omegar.mvp.presenter.InjectPresenter;

/**
 * Date: 18-Dec-15
 * Time: 13:16
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
public class MvpProcessor {
	public static final String PRESENTER_BINDER_SUFFIX = "$$PresentersBinder";
	public static final String PRESENTER_BINDER_INNER_SUFFIX = "Binder";
	public static final String VIEW_STATE_SUFFIX = "$$State";
	public static final String VIEW_STATE_PROVIDER_SUFFIX = "$$ViewStateProvider";

	/**
	 * <p>1) Generates tag for identification MvpPresenter</p>
	 * <p>2) Checks if presenter with tag is already exist in {@link PresenterStore}, and returns it</p>
	 * <p>3) If {@link PresenterStore} doesn't contain MvpPresenter with current tag, {@link PresenterField} will create it</p>
	 *
	 * @param <Delegated>    type of delegated
	 * @param target         object that want injection
	 * @param presenterField info about presenter from {@link InjectPresenter}
	 * @param delegateTag    unique tag @return MvpPresenter instance
	 */
	private <Delegated> MvpPresenter<? super Delegated> getMvpPresenter(Delegated target,
																		PresenterField<Delegated> presenterField,
																		String delegateTag) {
		PresenterStore presenterStore = MvpFacade.getInstance().getPresenterStore();

		PresenterType type = presenterField.getPresenterType();
		String tag;
		if (type == PresenterType.LOCAL) {
			tag = delegateTag + "$" + presenterField.getTag(target);
		} else {
			tag = presenterField.getTag(target);
		}

		//noinspection unchecked
		MvpPresenter<? super Delegated> presenter = presenterStore.get(tag);
		if (presenter != null) {
			return presenter;
		}

		//noinspection unchecked
		presenter = (MvpPresenter<? super Delegated>) presenterField.providePresenter(target);

		if (presenter == null) {
			return null;
		}

		presenter.setPresenterType(type);
		presenter.setTag(tag);
		presenterStore.add(tag, presenter);

		return presenter;
	}

	/**
	 * <p>Gets presenters {@link List} annotated with {@link InjectPresenter} for view.</p>
	 * <p>See full info about getting presenter instance in {@link #getMvpPresenter}</p>
	 *
	 * @param delegated   class contains presenter
	 * @param delegateTag unique tag
	 * @param <Delegated> type of delegated
	 * @return presenters list for specifies presenters container
	 */
	<Delegated> List<MvpPresenter<? super Delegated>> getMvpPresenters(Delegated delegated,
																	   String delegateTag,
																	   List<PresenterField<Delegated>> customPresenterFields) {
		List<MvpPresenter<? super Delegated>> presenters = new ArrayList<>();

		List<Object> presenterBinders = getPresenterBinders(delegated);

		for (Object presenterBinderObject : presenterBinders) {
			//noinspection unchecked
			PresenterBinder<Delegated> presenterBinder = (PresenterBinder<Delegated>) presenterBinderObject;
			List<PresenterField<Delegated>> presenterFields = presenterBinder.getPresenterFields();

			for (PresenterField<Delegated> presenterField : presenterFields) {
				handlePresenterField(delegated, delegateTag, presenters, presenterField);
			}
		}

		// handle custom presenter fields
		if (customPresenterFields != null) {
			for (PresenterField<Delegated> presenterField : customPresenterFields) {
				handlePresenterField(delegated, delegateTag, presenters, presenterField);
			}
		}

		return presenters;
	}

	private <Delegated> List<Object> getPresenterBinders(Delegated delegated) {
		if (!hasMoxyReflector()) return Collections.emptyList();

		@SuppressWarnings("unchecked")
		Class<? super Delegated> aClass = (Class<Delegated>) delegated.getClass();
		List<Object> presenterBinders = null;

		while (aClass != Object.class && presenterBinders == null) {
			presenterBinders = MoxyReflector.getPresenterBinders(aClass);

			aClass = aClass.getSuperclass();
		}

		if (presenterBinders == null || presenterBinders.isEmpty()) {
			return Collections.emptyList();
		}
		return presenterBinders;
	}

	private <Delegated> void handlePresenterField(Delegated delegated, String delegateTag, List<MvpPresenter<? super Delegated>> presenters, PresenterField<Delegated> presenterField) {
		PresentersCounter presentersCounter = MvpFacade.getInstance().getPresentersCounter();

		MvpPresenter<? super Delegated> presenter = getMvpPresenter(delegated, presenterField, delegateTag);

		if (presenter != null) {
			presentersCounter.injectPresenter(presenter, delegateTag);
			presenters.add(presenter);
			presenterField.bind(delegated, presenter);
		}
	}

	private static Boolean hasMoxyReflector = null;

	// Check is it have generated MoxyReflector without usage of reflection API
	private static boolean hasMoxyReflector() {
		if (hasMoxyReflector != null) {
			return hasMoxyReflector;
		}

		try {
			new MoxyReflector();

			hasMoxyReflector = true;
		} catch (NoClassDefFoundError error) {
			hasMoxyReflector = false;
		}

		return hasMoxyReflector;
	}
}
