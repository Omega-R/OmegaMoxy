package com.omegar.mvp;

/**
 * Date: 17-Dec-15
 * Time: 19:00
 *
 * @author Alexander Blinov
 * @author Yuri Shmakov
 */
public enum MvpFacade {
	INSTANCE;

	public static MvpFacade getInstance() {
		return INSTANCE;
	}

	private PresenterStore mPresenterStore = new PresenterStore();
	private MvpProcessor mMvpProcessor = new MvpProcessor();
	private PresentersCounter mPresentersCounter = new PresentersCounter();

	public PresenterStore getPresenterStore() {
		return mPresenterStore;
	}

	@SuppressWarnings("unused")
	public void setPresenterStore(PresenterStore presenterStore) {
		mPresenterStore = presenterStore;
	}

	public MvpProcessor getMvpProcessor() {
		return mMvpProcessor;
	}

	@SuppressWarnings("unused")
	public void setMvpProcessor(MvpProcessor mvpProcessor) {
		mMvpProcessor = mvpProcessor;
	}

	public PresentersCounter getPresentersCounter() {
		return mPresentersCounter;
	}

	@SuppressWarnings("unused")
	public void setPresentersCounter(PresentersCounter presentersCounter) {
		mPresentersCounter = presentersCounter;
	}

}
