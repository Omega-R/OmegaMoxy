package com.omegar.mvp;

import com.omegar.mvp.presenter.PresenterField;
import com.omegar.mvp.presenter.PresenterType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Date: 18-Dec-15
 * Time: 13:51
 * <p>
 * This class represents a delegate which you can use to extend Mvp's support to any class.
 * <p>
 * When using an {@link MvpDelegate}, lifecycle methods which should be proxied to the delegate:
 * <ul>
 * <li>{@link #onCreate(MvpSaveStore)}</li>
 * <li>{@link #onAttach()}: inside onStart() of Activity or Fragment</li>
 * <li>{@link #onSaveInstanceState(MvpSaveStore)}</li>
 * <li>{@link #onDetach()}: inside onDestroy() for Activity or onDestroyView() for Fragment</li>
 * <li>{@link #onDestroy()}</li>
 * </ul>
 * <p>
 * Every {@link Object} can only be linked with one {@link MvpDelegate} instance,
 * so the instance returned from {@link #MvpDelegate(Object)}} should be kept
 * until the Object is destroyed.
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 * @author Konstantin Tckhovrebov
 */
@SuppressWarnings("rawtypes")
public class MvpDelegate<Delegated> {
	private static final String KEY_TAG = "MvpDelegate.KEY_TAG";
	public static final String MOXY_DELEGATE_BUNDLE_KEY = "MoxyDelegateBundle";
	public static final String MOXY_PRESENTER_BUNDLE_KEY = "MoxyPresenterBundle";

	private final List<PresenterField<Delegated>> mCustomPresenterFields = new ArrayList<>();

	private String delegateTag;
	private final Delegated delegated;
	private boolean isAttached;
	private List<MvpPresenter<? super Delegated>> presenters;
	@Nullable
	private MvpSaveStore saveStore;

	private int uniqueKey = hashCode();
	private boolean autoCreate = false;

	public MvpDelegate(Delegated delegated) {
		this.delegated = delegated;
	}

	public MvpDelegate(Delegated delegated, boolean autoCreate) {
		this(delegated);
		this.autoCreate = autoCreate;
	}

	public void setUniqueKey(final int uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public int getUniqueKey() {
		return uniqueKey;
	}

	public void enableAutoCreate() {
		autoCreate = true;
	}

	public void disableAutoCreate() {
		autoCreate = false;
	}

	public void autoCreate() {
		if (autoCreate && uniqueKey != hashCode()) {
			onCreate(null);
		}
	}

	/**
	 * <p>Get(or create if not exists) presenters for delegated object and bind
	 * them to this object fields</p>
	 *
	 * @param saveStore with saved state
	 */
	public void onCreate(@Nullable MvpSaveStore saveStore) {
		if (saveStore != null) {
			saveStore = saveStore.getKeyStore(MOXY_DELEGATE_BUNDLE_KEY);
		}
		isAttached = false;
		this.saveStore = saveStore;

		//get base tag for presenters
		delegateTag = (saveStore != null && saveStore.containsKey(KEY_TAG)) ? saveStore.getString(KEY_TAG) : generateTag();

		//bind presenters to view
		presenters = MvpFacade.getInstance().getMvpProcessor().getMvpPresenters(delegated, delegateTag, mCustomPresenterFields);
	}

	/**
	 * <p>Attach delegated object as view to presenter fields of this object.
	 * If delegate did not enter at {@link #onCreate(MvpSaveStore)}(or
	 * {@link #onCreate(MvpSaveStore)}) before this method, then view will not be attached to
	 * presenters</p>
	 */
	public void onAttach() {
		for (MvpPresenter<? super Delegated> presenter : presenters) {
			if (isAttached && presenter.getAttachedViews().contains(delegated)) {
				continue;
			}

			presenter.attachView(delegated);
		}

		isAttached = true;
	}

	/**
	 * <p>Detach delegated object from their presenters.</p>
	 */
	public void onDetach() {
		for (MvpPresenter<? super Delegated> presenter : presenters) {
			if (!isAttached && !presenter.getAttachedViews().contains(delegated)) {
				continue;
			}

			presenter.detachView(delegated);
		}

		isAttached = false;
	}

	/**
	 * <p>View was being destroyed, but logical unit still alive</p>
	 */
	public void onDestroyView() {
		for (MvpPresenter<? super Delegated> presenter : presenters) {
			presenter.destroyView(delegated);
		}
	}

	/**
	 * <p>Destroy presenters.</p>
	 */
	public void onDestroy() {
		PresentersCounter presentersCounter = MvpFacade.getInstance().getPresentersCounter();
		PresenterStore presenterStore = MvpFacade.getInstance().getPresenterStore();

		Set<MvpPresenter> allChildPresenters = presentersCounter.getAll(delegateTag);
		for (MvpPresenter presenter : allChildPresenters) {
			boolean isRejected = presentersCounter.rejectPresenter(presenter, delegateTag);
			if (isRejected && presenter.getPresenterType() != PresenterType.GLOBAL) {
				presenterStore.remove(presenter.getTag());
				presenter.onDestroy();
			}
		}
	}


	/**
	 * Save presenters tag prefix to save state for restore presenters at future after delegate recreate
	 *
	 * @param outState out state from Android component
	 */
	public void onSaveInstanceState(@NotNull MvpSaveStore outState) {
		outState = outState.putKeyStore(MOXY_DELEGATE_BUNDLE_KEY);
		if (saveStore != null) {
			outState.putAll(saveStore);
		}
		outState.putString(KEY_TAG, delegateTag);
	}

	@SuppressWarnings("unused")
	public MvpSaveStore getChildrenSaveState() {
		return saveStore;
	}

	public void addCustomPresenterFields(PresenterField<Delegated> customPresenterField) {
		mCustomPresenterFields.add(customPresenterField);
		autoCreate();
	}

	/**
	 * @return generated tag in format: &lt;parent_delegate_tag&gt; &lt;delegated_class_full_name&gt;$MvpDelegate@&lt;hashCode&gt;
	 * <p>
	 * example: SampleFragment$MvpDelegate@32649b0
	 */
	private String generateTag() {
		return delegated.getClass().getSimpleName() + "$" + getClass().getSimpleName() + "@" + Integer.toHexString(uniqueKey);
	}
}
