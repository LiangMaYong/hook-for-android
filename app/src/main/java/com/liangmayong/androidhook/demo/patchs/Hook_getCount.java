package com.liangmayong.androidhook.demo.patchs;

import com.liangmayong.androidhook.Hook;

import java.lang.reflect.Method;

/**
 * Created by 007 on 2016/7/17.
 */
public class Hook_getCount extends Hook {
    @Override
    public String getMethodName() {
        return "getCount";
    }

    @Override
    public Object onHook(Object who, Method method, Object... args) throws Throwable {
        return 99;
    }
}
