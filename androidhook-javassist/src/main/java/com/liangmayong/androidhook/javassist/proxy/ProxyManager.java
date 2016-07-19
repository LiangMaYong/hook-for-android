package com.liangmayong.androidhook.javassist.proxy;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import dalvik.system.DexClassLoader;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.android.DexFile;

public final class ProxyManager {

    private static boolean isInit = false;
    private static String DEX_FILE_DIR = "";
    private static Application application;

    public static boolean isInit() {
        return isInit;
    }

    public static void clearProxy() {
        if (!isInit) {
            return;
        }
        File file = new File(getDexFileDir());
        if (file.exists())
            file.delete();
    }

    /**
     * init
     *
     * @param application application
     */
    public static void init(Application application) {
        if (isInit) {
            return;
        }
        File file = application.getDir("androidproxy", Context.MODE_PRIVATE);
        if (!file.exists()) {
            file.mkdirs();
        }
        ProxyManager.application = application;
        DEX_FILE_DIR = file.getAbsolutePath();
        ProxyManager.isInit = true;
    }

    /**
     * getDexCacheDir
     *
     * @return dexcache dir
     */
    public static String getDexCacheDir() {
        if (!isInit) {
            return "";
        }
        return application.getCacheDir().getAbsolutePath();
    }

    /**
     * getClassLoader
     *
     * @return classloader
     */
    public static ClassLoader getClassLoader() {
        if (!isInit) {
            return ClassLoader.getSystemClassLoader();
        }
        return application.getClassLoader();
    }

    /**
     * getDexFileDir
     *
     * @return dexfile dir
     */
    public static String getDexFileDir() {
        if (!isInit) {
            return "";
        }
        return DEX_FILE_DIR;
    }

    /**
     * getClassPool
     *
     * @return pool
     */
    public static ClassPool getClassPool() {
        if (!isInit) {
            return null;
        }
        try {
            ClassPool pool = ClassPool.getDefault(application.getApplicationContext());
            return pool;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Object newProxyInstance(Class<?> targetClass,
                                          ProxyInvocationHandler interceptor) {
        return newProxyInstance(null, targetClass, interceptor);
    }

    /**
     * newProxyInstance
     *
     * @param classLoader classLoader
     * @param targetClass targetClass
     * @param interceptor interceptor
     *                    force
     * @return instance
     */
    public static Object newProxyInstance(ClassLoader classLoader, Class<?> targetClass,
                                          ProxyInvocationHandler interceptor) {
        if (!ProxyManager.isInit()) {
            return null;
        }
        String classname = getProxyClassname(targetClass);
        final File dexFile = getDexFile(targetClass);
        if (!dexFile.exists()) {
            createProxy(targetClass);
        }
        // get instance
        if (dexFile.exists()) {
            try {
                final DexClassLoader dcl = new DexClassLoader(dexFile.getAbsolutePath(), ProxyManager.getDexCacheDir(),
                        null, classLoader == null ? ProxyManager.getClassLoader() : classLoader);
                final Class<?> proxyClass = dcl.loadClass(classname);
                Class<?>[] parameterTypes = getParameterTypes(targetClass);
                final Constructor<?> ctor = proxyClass.getConstructor(parameterTypes);
                final Object obj = ctor.newInstance(new Object[parameterTypes.length]);
                try {
                    final Method m = obj.getClass().getDeclaredMethod("setInterceptor", ProxyInvocationHandler.class);
                    m.invoke(obj, interceptor);
                } catch (Exception e) {
                }
                return obj;
            } catch (Exception e) {
            }
        }
        return null;
    }

    /**
     * createProxy
     *
     * @param targetClass targetClass
     */
    public static void createProxy(Class<?> targetClass) {
        if (!ProxyManager.isInit()) {
            return;
        }
        String classname = getProxyClassname(targetClass);
        final File dexFile = getDexFile(targetClass);
        if (dexFile.exists()) {
            dexFile.mkdirs();
        }
        int index = 0;
        List<String> methodstr = new ArrayList<String>();
        ClassPool pool = ProxyManager.getClassPool();
        try {
            final CtClass proxy = pool.makeClass(classname);
            CtClass superclass = pool.get(targetClass.getName());
            proxy.setSuperclass(superclass);
            Class<?>[] parameterTypes = getParameterTypes(targetClass);
            CtConstructor ctor = null;
            if (parameterTypes != null && parameterTypes.length > 0) {
                CtClass[] ctparameterTypes = new CtClass[parameterTypes.length];
                for (int i = 0; i < parameterTypes.length; i++) {
                    ctparameterTypes[i] = pool.get(parameterTypes[i].getName());
                }
                ctor = new CtConstructor(ctparameterTypes, proxy);
                StringBuffer buffer = new StringBuffer();
                buffer.append("{\n");
                buffer.append("super(");
                for (int i = 0; i < ctparameterTypes.length; i++) {
                    buffer.append("$" + (i + 1));
                    if (i < ctparameterTypes.length - 1) {
                        buffer.append(",");
                    }
                }
                buffer.append(");\n");
                buffer.append("}");
                ctor.setBody(buffer.toString());
            } else {
                ctor = new CtConstructor(null, proxy);
                ctor.setBody("{super();}");
            }
            proxy.addConstructor(ctor);

            // insert interceptor
            CtField interceptorField = CtField
                    .make("private " + ProxyInvocationHandler.class.getName() + " interceptor;", proxy);
            proxy.addField(interceptorField);
            proxy.addMethod(CtNewMethod.setter("setInterceptor", interceptorField));

            // insert methods

            Class<?> clazzSuper = targetClass;
            for (; clazzSuper != Object.class; clazzSuper = clazzSuper.getSuperclass()) {
                CtClass clazz = pool.get(clazzSuper.getName());

                // insert proxy
                CtMethod[] methods = clazz.getDeclaredMethods();
                for (int i = 0; i < methods.length; i++) {
                    CtMethod m = methods[i];
                    String methodname = m.getName() + m.getReturnType().getName();
                    CtClass[] classes = m.getParameterTypes();
                    for (int j = 0; j < classes.length; j++) {
                        methodname += "," + classes[j].getName();
                    }
                    if (methodstr.contains(methodname)) {
                        break;
                    }
                    methodstr.add(methodname);
                    m.setModifiers(Modifier.PUBLIC);
                    StringBuilder fields = new StringBuilder();
                    fields.append("private static java.lang.reflect.Method method" + index);
                    fields.append("=Class.forName(\"");
                    fields.append(clazz.getName());
                    fields.append("\").getDeclaredMethods()[");
                    fields.append(i);
                    fields.append("];");
                    CtField cf = CtField.make(fields.toString(), proxy);
                    proxy.addField(cf);
                    generateMethods(pool, proxy, m, m.getReturnType().getName(), index);
                    index++;
                }
            }
            proxy.writeFile(ProxyManager.getDexFileDir());
            final DexFile df = new DexFile();
            final String dexFilePath = dexFile.getAbsolutePath();
            df.addClass(new File(ProxyManager.getDexFileDir(), classname + ".class"));
            df.writeFile(dexFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getParameterTypes
     *
     * @param targetClass targetClass
     * @return parameterTypes
     */
    private static Class<?>[] getParameterTypes(Class<?> targetClass) {
        Constructor<?>[] cons = targetClass.getDeclaredConstructors();
        Constructor<?> con = cons[0];
        return con.getParameterTypes();
    }

    /**
     * getDexFile
     *
     * @param targetClass targetClass
     * @return dexfile
     */
    public static File getDexFile(Class<?> targetClass) {
        return new File(ProxyManager.getDexFileDir(), getProxyClassname(targetClass) + ".dex");
    }

    /**
     * getProxyClassname
     *
     * @param targetClass targetClass
     * @return class name
     */
    public static String getProxyClassname(Class<?> targetClass) {
        return encrypt(targetClass.getName());
    }

    /**
     * generateMethods
     *
     * @param pool       pool
     * @param proxy      proxy
     * @param method     method
     * @param returnType returnType
     * @param index      index
     */
    private static void generateMethods(ClassPool pool, CtClass proxy, CtMethod method, String returnType, int index) {
        try {
            CtMethod cm = new CtMethod(method.getReturnType(), method.getName(), method.getParameterTypes(), proxy);
            StringBuilder mbody = new StringBuilder();
            if ("void".equals(returnType)) {
                mbody.append("{this.interceptor.invoke(this,method");
                mbody.append(index);
                mbody.append(",$args);}");
                cm.setBody(mbody.toString());
            } else {
                mbody.append("{return (");
                mbody.append(returnType);
                mbody.append(")this.interceptor.invoke(this,method");
                mbody.append(index);
                mbody.append(",$args);}");
                cm.setBody(mbody.toString());
            }
            proxy.addMethod(cm);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * MD5 encrypt
     *
     * @param str string
     * @return encrypt string
     */
    @SuppressLint("DefaultLocale")
    private final static String encrypt(String str) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            byte[] strTemp = str.getBytes();
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(strTemp);
            byte tmp[] = mdTemp.digest();
            char strs[] = new char[16 * 2];
            int k = 0;
            for (int i = 0; i < 16; i++) {
                byte byte0 = tmp[i];
                strs[k++] = hexDigits[byte0 >>> 4 & 0xf];
                strs[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(strs).toUpperCase();
        } catch (Exception e) {
            return null;
        }
    }
}
