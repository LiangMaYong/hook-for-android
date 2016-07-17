package com.liangmayong.androidhook;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * HookObject
 *
 * @param <T> T
 * @author LiangMaYong
 * @version 1.0
 */
public class HookObject<T> {

    protected T mBaseObject;
    protected T mProxyObject;

    private Map<String, Hook> internalHookMapping = new HashMap<String, Hook>();

    public HookObject(T baseObject, Class<?>... proxyInterfaces) {
        this(baseObject == null ? null : baseObject.getClass().getClassLoader(), baseObject, proxyInterfaces);
    }

    public HookObject(ClassLoader cl, T baseObject, Class<?>... proxyInterfaces) {
        this.mBaseObject = baseObject;
        if (mBaseObject != null) {
            if (proxyInterfaces == null) {
                proxyInterfaces = baseObject.getClass().getInterfaces();
            }
            mProxyObject = (T) Proxy.newProxyInstance(cl, proxyInterfaces, new HookHandler());
        }
    }

    public HookObject(T baseObject) {
        this(baseObject, (Class<?>[]) null);
    }

    public void addHook(Hook hook) {
        if (hook != null && hook.getMethodName() != null && !"".equals(hook.getMethodName())) {
            internalHookMapping.put(hook.getMethodName(), hook);
        }
    }

    /**
     * removeHook
     *
     * @param hookName hookName
     * @return hook
     */
    public Hook removeHook(String hookName) {
        return internalHookMapping.remove(hookName);
    }

    /**
     * removeHook
     *
     * @param hook hook
     */
    public void removeHook(Hook hook) {
        if (hook != null) {
            removeHook(hook.getMethodName());
        }
    }

    /**
     * removeAllHook
     */
    public void removeAllHook() {
        internalHookMapping.clear();
    }

    /**
     * getHook
     *
     * @param name name
     * @param <H>  hook
     * @return hook
     */
    public <H extends Hook> H getHook(String name) {
        return (H) internalHookMapping.get(name);
    }

    /**
     * getProxyObject
     *
     * @return HookProxy
     */
    public T getProxyObject() {
        return mProxyObject;
    }

    /**
     * getBaseObject
     *
     * @return base
     */
    public T getBaseObject() {
        return mBaseObject;
    }

    /**
     * getHookCount
     *
     * @return hook count
     */
    public int getHookCount() {
        return internalHookMapping.size();
    }

    /**
     * HookHandler
     */
    private class HookHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Hook hook = getHook(method.getName());
            if (hook != null && hook.isEnable()) {
                //befor
                hook.beforeMethod(mBaseObject, method, args);
                //hook
                Object result = hook.onHook(mBaseObject, method, args);
                //after
                hook.afterMethod(mBaseObject, method, args);
                return result;
            }
            Object result = method.invoke(mBaseObject, args);
            return result;
        }
    }


}
