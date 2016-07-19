package com.liangmayong.androidhook;

import com.liangmayong.androidhook.javassist.proxy.ProxyHandler;
import com.liangmayong.androidhook.javassist.proxy.ProxyInvocationHandler;

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
    protected Object mProxyObject;
    private boolean javassist_enable = false;

    /**
     * isJavassistEnable
     *
     * @return javassist_enable
     */
    public boolean isJavassistEnable() {
        return javassist_enable;
    }

    // hook mapping
    private Map<String, Hook> internalHookMapping = new HashMap<String, Hook>();

    /**
     * HookObject
     *
     * @param baseObject      baseObject
     * @param proxyInterfaces proxyInterfaces
     */
    public HookObject(T baseObject, Class<?>... proxyInterfaces) {
        createProxy(baseObject, proxyInterfaces);
    }

    /**
     * HookObject
     *
     * @param cl              cl
     * @param baseObject      baseObject
     * @param proxyInterfaces proxyInterfaces
     */
    public HookObject(final ClassLoader cl, final T baseObject, Class<?>... proxyInterfaces) {
        createProxy(cl, baseObject, proxyInterfaces);
    }


    /**
     * HookObject
     *
     * @param baseObject baseObject
     */
    public HookObject(T baseObject) {
        createProxy(baseObject, (Class<?>[]) null);
    }

    /**
     * HookObject
     *
     * @param baseObject baseObject
     */
    public HookObject(T baseObject, boolean javassist) {
        javassist_enable = javassist;
        createProxy(baseObject, (Class<?>[]) null);
    }

    public void createProxy(T baseObject, Class<?>... proxyInterfaces) {
        createProxy(baseObject == null ? null : baseObject.getClass().getClassLoader(), baseObject, proxyInterfaces);
    }

    public void createProxy(final ClassLoader cl, final T baseObject, Class<?>... proxyInterfaces) {
        this.mBaseObject = baseObject;
        if (mBaseObject != null) {
            if (proxyInterfaces == null) {
                proxyInterfaces = baseObject.getClass().getInterfaces();
            }
            if (isJavassistEnable()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mProxyObject = ProxyHandler.newProxyInstance(cl, baseObject.getClass(), new HookHandler());
                    }
                }).start();
            } else {
                mProxyObject = Proxy.newProxyInstance(cl, proxyInterfaces, new HookHandler());
            }
        }
    }

    /**
     * addHook
     *
     * @param hook hook
     */
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
    public Object getProxyInterface() {
        return mProxyObject;
    }

    /**
     * getProxy
     *
     * @return HookProxy
     */
    public T getProxy() {
        return (T) mProxyObject;
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
    private class HookHandler implements ProxyInvocationHandler {

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
