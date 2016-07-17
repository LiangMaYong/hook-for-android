package com.liangmayong.androidhook;

import java.lang.reflect.Method;

/**
 * Hook
 *
 * @author LiangMaYong
 * @version 1.0
 */
public abstract class Hook {

    private boolean enable = true;

    public Hook() {
    }

    /**
     * getMethodName
     *
     * @return method name
     */
    public abstract String getMethodName();

    /**
     * beforeMethod
     *
     * @param who    who
     * @param method method
     * @param args   args
     */
    public void beforeMethod(Object who, Method method, Object... args) {
    }

    /**
     * afterMethod
     *
     * @param who    who
     * @param method method
     * @param args   args
     */
    public void afterMethod(Object who, Method method, Object... args) {
    }

    /**
     * onHook
     *
     * @param who    who
     * @param method method
     * @param args   args
     * @return result
     * @throws Throwable th
     */
    public abstract Object onHook(Object who, Method method, Object... args) throws Throwable;

    /**
     * isEnable
     *
     * @return enable
     */
    public boolean isEnable() {
        return enable;
    }

    /**
     * setEnable
     *
     * @param enable enable
     */
    public void setEnable(boolean enable) {
        this.enable = enable;
    }

}
