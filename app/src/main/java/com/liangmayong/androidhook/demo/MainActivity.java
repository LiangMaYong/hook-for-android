package com.liangmayong.androidhook.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.liangmayong.androidhook.javassist.proxy.ProxyInvocationHandler;
import com.liangmayong.androidhook.javassist.proxy.ProxyManager;

import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TAG", "onCreate");
        ProxyManager.init(getApplication());
        setContentView(R.layout.activity_main);
        demo();
    }

    private void demo() {
        final DemoImpl i = new DemoImpl();
        DemoImpl demo = (DemoImpl) ProxyManager.newProxyInstance(DemoImpl.class, new ProxyInvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (method.getName().equals("getCount")) {
                    return 500;
                }
                return method.invoke(i, args);
            }
        });
        Log.d("TAG", demo.getCount() + "");
    }
}
