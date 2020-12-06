package com.omegar.mvp.compiler.pipeline;

import java.io.Serializable;

/**
 * Created by Anton Knyazev on 06.12.2020.
 */

public class Quad<FI,SE,TH,FO> implements Serializable {

    private final FI mFirst;
    private final SE mSecond;
    private final TH mThird;
    private final FO mFourth;

    public Quad(FI first, SE second, TH third, FO fourth) {
        mFirst = first;
        mSecond = second;
        mThird = third;
        mFourth = fourth;
    }

    public FI getFirst() {
        return mFirst;
    }

    public SE getSecond() {
        return mSecond;
    }

    public TH getThird() {
        return mThird;
    }

    public FO getFourth() {
        return mFourth;
    }

    @Override
    public String toString() {
        return "Quad{" +
                "first=" + mFirst +
                ", second=" + mSecond +
                ", third=" + mThird +
                ", fourth=" + mFourth +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Quad<?, ?, ?, ?> quad = (Quad<?, ?, ?, ?>) o;

        if (mFirst != null ? !mFirst.equals(quad.mFirst) : quad.mFirst != null) return false;
        if (mSecond != null ? !mSecond.equals(quad.mSecond) : quad.mSecond != null) return false;
        if (mThird != null ? !mThird.equals(quad.mThird) : quad.mThird != null) return false;
        return mFourth != null ? mFourth.equals(quad.mFourth) : quad.mFourth == null;
    }

    @Override
    public int hashCode() {
        int result = mFirst != null ? mFirst.hashCode() : 0;
        result = 31 * result + (mSecond != null ? mSecond.hashCode() : 0);
        result = 31 * result + (mThird != null ? mThird.hashCode() : 0);
        result = 31 * result + (mFourth != null ? mFourth.hashCode() : 0);
        return result;
    }
}
