package com.omegar.mvp;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;

/**
 * Date: 17-Dec-15
 * Time: 16:05
 *
 * @author Yuri Shmakov
 * @author Alexander Blinov
 */
@SuppressWarnings("rawtypes")
public class PresenterStore {
	private final Map<String, MvpPresenter> mPresenters = new HashMap<>();

	/**
	 * Add presenter to storage
	 *
	 * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
	 * @param instance Instance of MvpPresenter implementation to store
	 * @param <T>      Type of presenter
	 */
	public <T extends MvpPresenter> void add(String tag, T instance) {
		mPresenters.put(tag, instance);
	}

	/**
	 * Get presenter on existing params
	 *
	 * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
	 * @return         Presenter if it's exists. Null otherwise (if it's no exists)
	 */
	public MvpPresenter get(String tag) {
		return mPresenters.get(tag);
	}

	/**
	 * Remove presenter from store.
	 *
	 * @param tag      Tag of presenter. Local presenters contains also delegate's tag as prefix
	 * @return         Presenter which was removed
	 */
	public MvpPresenter remove(String tag) {
		return mPresenters.remove(tag);
	}

	@SuppressWarnings("unused")
	public void logPresenters() {
		for (Map.Entry<String, MvpPresenter> currentEntry : mPresenters.entrySet()) {
			Log.d("PresenterStore", currentEntry.getKey() + " -> " +
			                        currentEntry.getValue());
		}
	}
}