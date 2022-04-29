package com.omegar.mvp.viewstate;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import com.omegar.mvp.MvpView;
import com.omegar.mvp.viewstate.strategy.StateStrategy;

/**
 * Date: 15.12.2015
 * Time: 19:58
 *
 * @author Yuri Shmakov
 */
public abstract class MvpViewState<View extends MvpView> {
	protected final ViewCommands<View> mViewCommands = new ViewCommands<>();
	protected final Set<View> mViews = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());
	protected final Set<View> mInRestoreState = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());
	protected final Map<View, Set<ViewCommand<View>>> mViewStates = new WeakHashMap<>();

	protected void apply(ViewCommand<View> command) {
		mViewCommands.beforeApply(command);

		if (mViews.isEmpty()) {
			return;
		}

		for (View view$ : mViews) {
			command.apply(view$);
		}

		mViewCommands.afterApply(command);
	}

	@SuppressWarnings({"unchecked", "SameParameterValue"})
	protected <C extends ViewCommand<View>> C findCommand(Class<C> clz) {
		for (ViewCommand<?> viewCommand : mViewCommands.getCurrentState()) {
			if (clz.isInstance(viewCommand)) {
				return (C) viewCommand;
			}
		}
		return null;
	}

	/**
	 * Apply saved state to attached view
	 *
	 * @param view mvp view to restore state
	 * @param currentState commands that was applied already
	 */
	protected void restoreState(View view, Set<ViewCommand<View>> currentState) {
		if (mViewCommands.isEmpty()) {
			return;
		}

		mViewCommands.reapply(view, currentState);
	}

	/**
	 * Attach view to view state and apply saves state
	 *
	 * @param view attachment
	 */
	public void attachView(View view) {
		if (view == null) {
			throw new IllegalArgumentException("Mvp view must be not null");
		}

		boolean isViewAdded = mViews.add(view);

		if (!isViewAdded) {
			return;
		}

		mInRestoreState.add(view);

		Set<ViewCommand<View>> currentState = mViewStates.get(view);
		currentState = currentState == null ? Collections.<ViewCommand<View>>emptySet() : currentState;

		restoreState(view, currentState);

		mViewStates.remove(view);

		mInRestoreState.remove(view);
	}

	/**
	 * <p>Detach view from view state. After this moment view state save
	 * commands via
	 * {@link StateStrategy#beforeApply(List, ViewCommand)}.</p>
	 *
	 * @param view target mvp view to detach
	 */
	public void detachView(View view) {
		mViews.remove(view);
		mInRestoreState.remove(view);

		Set<ViewCommand<View>> currentState = Collections.newSetFromMap(new WeakHashMap<ViewCommand<View>, Boolean>());
		currentState.addAll(mViewCommands.getCurrentState());
		mViewStates.put(view, currentState);
	}

	public void destroyView(View view) {
		mViewStates.remove(view);
	}

	/**
	 * @return views, attached to this view state instance
	 */
	public Set<View> getViews() {
		return mViews;
	}

	/**
	 * Check if view is in restore state or not
	 *
	 * @param view view for check
	 * @return true if view state restore state to incoming view. false otherwise.
	 */
	public boolean isInRestoreState(View view) {
		return mInRestoreState.contains(view);
	}

	protected static String buildString(String name, Object... args) {
		StringBuilder builder = new StringBuilder(name);
		if (args.length > 0) {
			builder.append("{");
			boolean key = true;
			boolean isFirst = true;
			for(Object arg : args) {
				if (!isFirst) {
					if (key) {
						builder.append(", ");
					}
				} else {
					isFirst = false;
				}
				if (!key && arg instanceof String) {
					builder.append("'");
				}
				builder.append(arg);
				if (key) {
					builder.append("=");
				} else if (arg instanceof String) {
					builder.append("'");
				}
				key = !key;
			}
			builder.append("}");
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "MvpViewState{" +
				"viewCommands=" + mViewCommands +
				'}';
	}
}
