package example.com.moxy_androidx_sample;

import com.omegar.mvp.presenter.InjectPresenter;
import com.omegar.mvp.presenter.ProvidePresenter;

public class NotMainActivity extends BaseActivity implements NotMainView {

    @InjectPresenter
    NotMainPresenter mPresenter;


    @ProvidePresenter
    NotMainPresenter providePresenter() {
        return new NotMainPresenter() {};
    }

    @Override
    public void notMainFunction() {

    }

    @Override
    public void setTest(float test) {

    }

    @Override
    public float getTest() {
        return 0f;
    }
}
