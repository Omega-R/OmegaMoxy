package com.omegar.mvp;

import android.os.Bundle;

import androidx.annotation.ContentView;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;


/**
 * Date: 25-July-18
 * Time: 2:51
 *
 * @author Vova Stelmashchuk
 */
@SuppressWarnings({"unused", "rawtypes"})
public class MvpAppCompatActivity extends AppCompatActivity implements MvpDelegateHolder {
    private MvpDelegate<? extends MvpAppCompatActivity> mMvpDelegate;

    public MvpAppCompatActivity() { super(); }

    @ContentView
    public MvpAppCompatActivity(@LayoutRes int contentLayoutId) { super(contentLayoutId); }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getMvpDelegate().onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        getMvpDelegate().onAttach();
    }

    @Override
    protected void onResume() {
        super.onResume();

        getMvpDelegate().onAttach();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        getMvpDelegate().onSaveInstanceState(outState);
        getMvpDelegate().onDetach();
    }

    @Override
    protected void onStop() {
        super.onStop();

        getMvpDelegate().onDetach();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        getMvpDelegate().onDestroyView();

        if (isFinishing()) {
            getMvpDelegate().onDestroy();
        }
    }

    /**
     * @return The {@link MvpDelegate} being used by this Activity.
     */
    @Override
    public MvpDelegate getMvpDelegate() {
        if (mMvpDelegate == null) {
            mMvpDelegate = new MvpDelegate<>(this);
        }
        return mMvpDelegate;
    }
}
