package com.baidu.iknow.apkpatch;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class Main {

    /**
     * 工具的入口由 apkpatch.jar 的 main() 方法改为了 main.jar 的 main() 方法。
     * 所以在这里我们就可以动态替换相关的关键类和方法
     */
    public static void main(String[] args) {
        try {
            OriginLoader oldLoader = getOriginClassLoader(Main.class.getClassLoader());
            FixLoader newLoader = getFixClassLoader(Main.class.getClassLoader());
            oldLoader.otherClassLoder = newLoader;
            oldLoader.otherLoadClassNames = new ArrayList<String>();
            oldLoader.otherLoadClassNames.add("com.euler.patch.diff.DexDiffer");
            oldLoader.otherLoadClassNames.add("com.euler.patch.annotation.MethodReplaceAnnotaion");
            newLoader.otherClassLoder = oldLoader;

            // oldLoader 通过反射得到apkpatch.jar中的main()方法；
            // 根据 JVM 提供的类加载传导规则：JVM 会选择当前类的类加载器来加载所有该类的引用的类.
            // 得到，oldLoader 以后就是 Main类(这个 Java 程序的入口类)及其引用类的类加载器实例，如果间接引用到了
            // dexdiffer.jar 中的 DexDiffer.java 类，则通过 findClass() 会调用 FixLoader 加载器实例去加载.
            Class<?> oldMainClass = oldLoader.loadClass("com.euler.patch.Main");
            Method mainMethod = oldMainClass.getDeclaredMethod("main", String[].class);
            mainMethod.setAccessible(true);
            // 执行apkpatch.jar中的main()方法
            mainMethod.invoke(oldMainClass, (Object) args);
        } catch (ClassNotFoundException |
                NoSuchMethodException |
                SecurityException |
                IllegalAccessException |
                IllegalArgumentException |
                InvocationTargetException |
                MalformedURLException e) {

            e.printStackTrace();
        }
    }

    private static OriginLoader getOriginClassLoader(ClassLoader parent) throws MalformedURLException {
        URL[] urls = new URL[] {};
        OriginLoader loader = new OriginLoader(urls, parent);
        // 获取 Man 类编译完成后的 Main.class 生成路径，这里应该是：
        // /Users/habbyge/IdeaProjects/andfix_apkpatch_support_multdex/apkpatch/bin/，
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int pathLastIndex = path.lastIndexOf(File.separator) + 1;
        path = path.substring(0, pathLastIndex);
        path = path + "apkpatch.jar";
        // 让 OriginLoader（Classloader） 能够搜索到 apkpatch.jar 中的类和资源
        loader.addJar(new File(path).toURI().toURL());
        return loader;
    }
    
    private static FixLoader getFixClassLoader(ClassLoader parent) throws MalformedURLException {
        URL[] urls = new URL[] {};
        FixLoader loader = new FixLoader(urls, parent);
        String path = Main.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int pathLastIndex = path.lastIndexOf(File.separator) + 1;
        path = path.substring(0, pathLastIndex);
        path = path + "dexdiffer.jar";
        // 让 FixLoader（Classloader） 能够搜索到 dexdiffer.jar 中的类和资源
        loader.addJar(new File(path).toURI().toURL());
        return loader;
    }
}
