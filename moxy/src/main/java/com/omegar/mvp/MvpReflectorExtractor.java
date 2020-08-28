package com.omegar.mvp;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Anton Knyazev on 28.08.2020.
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class MvpReflectorExtractor {

    private final Class mCls;

    public MvpReflectorExtractor(String moxyReflectorClass) {
        Class cls;
        try {
            cls = Class.forName(moxyReflectorClass);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            cls = null;
        }
        mCls = cls;
    }

    public Map<Class<?>, Object> getViewStateProviders() {
        if (mCls != null) {
            Method m = null;
            try {
                m = mCls.getMethod("getViewStateProviders");
                return (Map<Class<?>, Object>) m.invoke(mCls);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }

    public Map<Class<?>, List<Object>> getPresenterBinders() {
        if (mCls != null) {
            try {

                Method m = mCls.getMethod("getPresenterBinders");
                return (Map<Class<?>, List<Object>>) m.invoke(mCls);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();
    }

    public Map<Class<?>, Object> getStrategies() {
        if (mCls != null) {
            try {
                Method m = mCls.getMethod("getStrategies");
                return (Map<Class<?>, Object>) m.invoke(mCls);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return Collections.emptyMap();

    }

}
