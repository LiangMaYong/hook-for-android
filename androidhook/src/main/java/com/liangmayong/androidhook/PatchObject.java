package com.liangmayong.androidhook;

import android.os.Build;

import com.liangmayong.androidhook.annotation.ApiLimit;
import com.liangmayong.androidhook.annotation.Patch;

import java.lang.reflect.Constructor;

/**
 * PatchObject
 *
 * @param <T> T
 * @author LiangMaYong
 * @version 1.0
 * @see Patch
 */
public abstract class PatchObject<T> {

    //hookObject
    protected HookObject<T> hookObject;

    //baseObject
    protected T baseObject;

    /**
     * PatchObject
     *
     * @param baseObject baseObject
     */
    public PatchObject(T baseObject) {
        this.baseObject = baseObject;
        this.hookObject = initHookObject(baseObject);
        applyHooks();
        afterHookApply(hookObject);
    }

    /**
     * getBaseObject
     *
     * @return t
     */
    public T getBaseObject() {
        return baseObject;
    }

    /**
     * getProxyObject
     *
     * @return t
     */
    public T getProxyObject() {
        return hookObject.getProxyObject();
    }

    /**
     * initHookObject
     *
     * @param baseObject baseObject
     * @return hook object
     */
    protected HookObject<T> initHookObject(T baseObject) {
        return new HookObject<T>(baseObject);
    }

    /**
     * applyHooks
     */
    protected void applyHooks() {

        if (hookObject != null) {
            Class<? extends PatchObject> clazz = getClass();
            Patch patch = clazz.getAnnotation(Patch.class);
            int version = Build.VERSION.SDK_INT;
            if (patch != null) {
                Class<? extends Hook>[] hookTypes = patch.value();
                for (Class<? extends Hook> hookType : hookTypes) {
                    ApiLimit apiLimit = hookType.getAnnotation(ApiLimit.class);
                    boolean needToAddHook = true;
                    if (apiLimit != null) {
                        int apiStart = apiLimit.start();
                        int apiEnd = apiLimit.end();
                        boolean highThanStart = apiStart == -1 || version > apiStart;
                        boolean lowThanEnd = apiEnd == -1 || version < apiEnd;
                        if (!highThanStart || !lowThanEnd) {
                            needToAddHook = false;
                        }
                    }
                    if (needToAddHook) {
                        addHook(hookType);
                    }
                }

            }
        }
    }

    /**
     * addHook
     *
     * @param hookType hookType
     */
    protected void addHook(Class<? extends Hook> hookType) {
        try {
            Constructor<?> constructor = hookType.getDeclaredConstructors()[0];
            if (!constructor.isAccessible()) {
                constructor.setAccessible(true);
            }
            Hook hook = (Hook) constructor.newInstance();
            hookObject.addHook(hook);
        } catch (Throwable e) {
            throw new RuntimeException("Unable to instance Hook : " + hookType + " :" + e.getMessage());
        }
    }

    /**
     * afterHookApply
     *
     * @param hookObject hookObject
     */
    protected void afterHookApply(HookObject<T> hookObject) {
    }

    /**
     * getHookObject
     *
     * @return hookObject
     */
    public HookObject<T> getHookObject() {
        return hookObject;
    }
}
