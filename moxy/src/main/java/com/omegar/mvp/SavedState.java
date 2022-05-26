package com.omegar.mvp;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SavedState<T> implements Parcelable {

    private static final List<SavedProvider> sSavedProviders = new ArrayList<SavedProvider>() {{
        add(ParcelableSavedProvider.INSTANCE);
        add(SerializableSavedProvider.INSTANCE);
    }};

    private final List<T> mList;

    public SavedState(List<T> list) {
        mList = new ArrayList<>(list.size());
        for (T item : list) {
            if (findProviderIndex(item) != -1) {
                mList.add(item);
            }
        }
    }

    protected SavedState(Parcel in) {
        int size = in.readInt();
        if (size == 0) {
            mList = Collections.emptyList();
        } else {
            ClassLoader classLoader = ((Class<?>) in.readSerializable()).getClassLoader();
            mList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                byte providerType = in.readByte();
                mList.add((T) sSavedProviders.get(providerType).load(in, classLoader));
            }
        }
    }

    public List<T> getList() {
        return mList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mList.size());
        if (!mList.isEmpty()) {
            dest.writeSerializable(mList.get(0).getClass());
            for (T item : mList) {
                int providerIndex = findProviderIndex(item);
                dest.writeByte((byte) providerIndex);
                sSavedProviders.get(providerIndex).save(dest, item, flags);
            }

        }
    }

    private static int findProviderIndex(Object item) {
        for (int i = 0; i < sSavedProviders.size(); i++) {
            if (sSavedProviders.get(i).can(item)) {
                return i;
            }
        }
        return -1;
    }

    public static void addProvider(SavedProvider provider) {
        sSavedProviders.add(provider);
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

}
