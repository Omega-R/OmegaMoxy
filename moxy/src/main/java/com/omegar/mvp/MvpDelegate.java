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
 * <li>{@link #onCreate(MvpKeyStore)}</li>
 * <li>{@link #onAttach()}: inside onStart() of Activity or Fragment</li>
 * <li>{@link #onSaveInstanceState(MvpKeyStore)}</li>
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

	private String keyTag = KEY_TAG;
	private String delegateTag;
	private final Delegated delegated;
	private boolean initAutoCreate;
	private boolean isAttached;
	private MvpDelegate parentDelegate;
	private List<MvpPresenter<? super Delegated>> presenters;
	private List<MvpDelegate> childDelegates = new ArrayList<>();
	@Nullable
	private MvpKeyStore<?> keyStore;

	public MvpDelegate(Delegated delegated, boolean canAutoCreate) {
		this.delegated = delegated;
		this.initAutoCreate = canAutoCreate;
	}

	public MvpDelegate(Delegated delegated) {
		this(delegated, false);
	}

	@SuppressWarnings("unused")
	public void setParentDelegate(MvpDelegate delegate, String childId) {
		if (keyStore != null) {
			throw new IllegalStateException("You should call setParentDelegate() before first onCreate()");
		}
		if (childDelegates != null && childDelegates.size() > 0) {
			throw new IllegalStateException("You could not set parent delegate when there are already has child presenters");
		}

		parentDelegate = delegate;
		keyTag = parentDelegate.keyTag + "$" + childId;

		delegate.addChildDelegate(this);
	}

	private void addChildDelegate(MvpDelegate delegate) {
		childDelegates.add(delegate);
	}

	private void removeChildDelegate(MvpDelegate delegate) {
		childDelegates.remove(delegate);
	}

	/**
	 * Free self link from children list (mChildDelegates) in parent delegate
	 * property mParentDelegate stay keep link to parent delegate for access to
	 * parent bundle for save state in {@link #onSaveInstanceState()}
	 */
	public void freeParentDelegate() {
		if (parentDelegate == null) {
			throw new IllegalStateException("You should call freeParentDelegate() before first setParentDelegate()");
		}

		parentDelegate.removeChildDelegate(this);
	}

	@SuppressWarnings("unused")
	public void removeAllChildDelegates() {
		// For avoiding ConcurrentModificationException when removing by removeChildDelegate()
		List<MvpDelegate> childDelegatesClone = new ArrayList<>(childDelegates.size());
		childDelegatesClone.addAll(childDelegates);

		for (MvpDelegate childDelegate : childDelegatesClone) {
			childDelegate.freeParentDelegate();
		}

		childDelegates = new ArrayList<>();
	}

	public void autoCreate() {
		if (initAutoCreate) {
			initAutoCreate = false;
			onCreate();
		}
	}

	/**
	 * <p>Similar like {@link #onCreate(MvpKeyStore)}. But this method try to get saved
	 * state from parent presenter before get presenters</p>
	 */
	public void onCreate() {
		MvpKeyStore bundle = parentDelegate != null ? parentDelegate.keyStore : null;
		onCreate(bundle);
	}

	/**
	 * <p>Get(or create if not exists) presenters for delegated object and bind
	 * them to this object fields</p>
	 *
	 * @param bundle with saved state
	 */
	public void onCreate(@Nullable MvpKeyStore bundle) {
		if (parentDelegate == null && bundle != null) {
			bundle = bundle.getKeyStore(MOXY_DELEGATE_BUNDLE_KEY);
		}

		isAttached = false;
		keyStore = bundle;

		//get base tag for presenters
		delegateTag = (keyStore != null && keyStore.containsKey(keyTag)) ? keyStore.getString(keyTag) : generateTag();

		//bind presenters to view
		presenters = MvpFacade.getInstance().getMvpProcessor().getMvpPresenters(delegated, delegateTag, mCustomPresenterFields);

		for (MvpDelegate childDelegate : childDelegates) {
			childDelegate.onCreate(bundle);
		}
	}

	/**
	 * <p>Attach delegated object as view to presenter fields of this object.
	 * If delegate did not enter at {@link #onCreate(MvpKeyStore)}(or
	 * {@link #onCreate()}) before this method, then view will not be attached to
	 * presenters</p>
	 */
	public void onAttach() {
		for (MvpPresenter<? super Delegated> presenter : presenters) {
			if (isAttached && presenter.getAttachedViews().contains(delegated)) {
				continue;
			}

			presenter.attachView(delegated);
		}

		for (MvpDelegate<?> childDelegate : childDelegates) {
			childDelegate.onAttach();
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

		for (MvpDelegate<?> childDelegate : childDelegates) {
			childDelegate.onDetach();
		}
	}

	/**
	 * <p>View was being destroyed, but logical unit still alive</p>
	 */
	public void onDestroyView() {
		for (MvpPresenter<? super Delegated> presenter : presenters) {
			presenter.destroyView(delegated);
		}

		// For avoiding ConcurrentModificationException when removing from mChildDelegates
		List<MvpDelegate> childDelegatesClone = new ArrayList<>(childDelegates.size());
		childDelegatesClone.addAll(childDelegates);

		for (MvpDelegate childDelegate : childDelegatesClone) {
			childDelegate.onSaveInstanceState();
			childDelegate.onDestroyView();
		}

		if (parentDelegate != null) {
			freeParentDelegate();
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
	 * <p>Similar like {@link #onSaveInstanceState(MvpKeyStore)}. But this method try to save
	 * state to parent presenter Bundle</p>
	 */
	public void onSaveInstanceState() {
		if (parentDelegate != null && parentDelegate.keyStore != null) {
			onSaveInstanceState(parentDelegate.keyStore);
		}
	}

	/**
	 * Save presenters tag prefix to save state for restore presenters at future after delegate recreate
	 *
	 * @param outState out state from Android component
	 */
	public void onSaveInstanceState(@NotNull MvpKeyStore outState) {
		if (parentDelegate == null) {
			outState = outState.putKeyStore(MOXY_DELEGATE_BUNDLE_KEY);
		}

		if (keyStore != null) {
			outState.putAll(keyStore);
		}
		outState.putString(keyTag, delegateTag);

		for (MvpDelegate childDelegate : childDelegates) {
			childDelegate.onSaveInstanceState(outState);
		}
	}

	@SuppressWarnings("unused")
	public MvpKeyStore getChildrenSaveState() {
		return keyStore;
	}

	public void addCustomPresenterFields(PresenterField<Delegated> customPresenterField, boolean canAutoCreate) {
		mCustomPresenterFields.add(customPresenterField);
		if (canAutoCreate) {
			autoCreate();
		}
	}

	/**
	 * @return generated tag in format: &lt;parent_delegate_tag&gt; &lt;delegated_class_full_name&gt;$MvpDelegate@&lt;hashCode&gt;
	 * <p>
	 * example: SampleFragment$MvpDelegate@32649b0
	 */
	private String generateTag() {
		String tag = parentDelegate != null ? parentDelegate.delegateTag + " " : "";
		tag += delegated.getClass().getSimpleName() + "$" + getClass().getSimpleName() + "@" + Integer.toHexString(hashCode());
		return tag;
	}
}
