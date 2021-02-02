package com.baidu.iknow.apkpatch;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

public class OriginLoader extends URLClassLoader {
    public FixLoader otherClassLoder; // 指向 dexdiffer.jar 中的ClassLoader
    public List<String> otherLoadClassNames; // 指向 dexdiffer.jar 中的 DexDiffer
    
    public OriginLoader(URL[] urls) {
        super(urls);
    }

    public OriginLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Appends the specified URL to the list of URLs to search for classes and resources.
     */
    public void addJar(URL url) {
        this.addURL(url);
    }

    /**
     * 这里非常重要，加载 apkpatch.jar 中的 DexDiffer 时，使用 FixLoader 加载 dexdiffer.jar 中的 DexDiffer，
     * 其他类的加载，依旧使用 OriginLoader。
     */
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (otherLoadClassNames.contains(name)) {
            return otherClassLoder.loadClass(name);
        }
        return super.findClass(name);
    }
}
