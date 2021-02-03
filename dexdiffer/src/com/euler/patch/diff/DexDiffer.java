package com.euler.patch.diff;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jf.dexlib2.DexFileFactory;
import org.jf.dexlib2.dexbacked.DexBackedClassDef;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.dexbacked.DexBackedField;
import org.jf.dexlib2.dexbacked.DexBackedMethod;
import org.jf.dexlib2.dexbacked.util.FixedSizeSet;

public class DexDiffer {

    public DiffInfo diff(File newFile, File oldFile) throws IOException {
//        DexBackedDexFile newDexFile = DexFileFactory.loadDexFile(newFile, "classes.dex", 19, true);
//        DexBackedDexFile newDexFile2 = DexFileFactory.loadDexFile(newFile, "classes2.dex", 19, true);
//        DexBackedDexFile oldDexFile = DexFileFactory.loadDexFile(oldFile, "classes.dex", 19, true);
//        DexBackedDexFile oldDexFile2 = DexFileFactory.loadDexFile(oldFile, "classes2.dex", 19, true);
//
//        FixedSizeSet<DexBackedClassDef> newclasses = (FixedSizeSet) newDexFile.getClasses();
//        FixedSizeSet<DexBackedClassDef> newclasses2 = (FixedSizeSet) newDexFile2.getClasses();
//        HashSet<DexBackedClassDef> newset = new HashSet<DexBackedClassDef>();
//        mergeHashSet(newset, newclasses);
//        mergeHashSet(newset, newclasses2);
//
//        FixedSizeSet<DexBackedClassDef> oldclasses = (FixedSizeSet) oldDexFile.getClasses();
//        FixedSizeSet<DexBackedClassDef> oldclasses2 = (FixedSizeSet) oldDexFile2.getClasses();
//        HashSet<DexBackedClassDef> oldset = new HashSet<DexBackedClassDef>();
//        mergeHashSet(oldset, oldclasses);
//        mergeHashSet(oldset, oldclasses2);

    	HashSet<DexBackedClassDef> newset = getClassSet(newFile);
    	HashSet<DexBackedClassDef> oldset = getClassSet(oldFile);
        DiffInfo info = DiffInfo.getInstance();

        boolean contains = false;
        Iterator<DexBackedClassDef> newIt = newset.iterator();
        while (newIt.hasNext()) {
            DexBackedClassDef newClass = newIt.next();
            Iterator<DexBackedClassDef> oldIt = oldset.iterator();
//            if (!isValidClassName(newClass.getType())) {
//        		continue;
//        	}
            contains = false;
            while (oldIt.hasNext()) {
                DexBackedClassDef oldClass = oldIt.next();
                if (newClass.equals(oldClass)) {
                    compareField(newClass, oldClass, info);
                    compareMethod(newClass, oldClass, info);
                    contains = true;
                    break;
                }
            }
            if (!contains) {
                info.addAddedClasses(newClass);
            }
        }
        return info;
    }
    
//    private boolean isValidClassName(String name) {
//    	if (name.contains("ParameterInjector") || name.contains("BindingStartup")) {
//    		return false;
//    	}
//    	return true;
//    }
//    
    private HashSet<DexBackedClassDef> getClassSet(File apkFile) throws IOException {
    	ZipFile localZipFile = new ZipFile(apkFile);
    	Enumeration<?> localEnumeration = localZipFile.entries();
    	HashSet<DexBackedClassDef> newset = new HashSet<DexBackedClassDef>();
    	while (localEnumeration.hasMoreElements()) {
			ZipEntry localZipEntry = (ZipEntry) localEnumeration.nextElement();
            // 所有以.dex结尾的文件都会加载。这样就支持的multidex
			if (localZipEntry.getName().endsWith(".dex")) {
				DexBackedDexFile newDexFile = DexFileFactory.loadDexFile(apkFile, localZipEntry.getName(), 19, true);
				FixedSizeSet<DexBackedClassDef> newclasses = (FixedSizeSet) newDexFile.getClasses();
				mergeHashSet(newset, newclasses);
			}
		}
    	return newset;
    }

    private void mergeHashSet(HashSet<DexBackedClassDef> set, FixedSizeSet<DexBackedClassDef> fset) {
        Iterator<DexBackedClassDef> tmpIter = fset.iterator();
        while (tmpIter.hasNext()) {
            DexBackedClassDef item = tmpIter.next();
            set.add(item);
        }
    }

    public void compareMethod(DexBackedClassDef newClass, DexBackedClassDef oldClass, DiffInfo info) {
        compareMethod(newClass.getMethods(), oldClass.getMethods(), info);
    }

    public void compareMethod(Iterable<? extends DexBackedMethod> news,
                              Iterable<? extends DexBackedMethod> olds, DiffInfo info) {

        for (DexBackedMethod newMehod : news)
            if (!newMehod.getName().equals("<clinit>")) {
                compareMethod(newMehod, olds, info);
            }
    }

    public void compareMethod(DexBackedMethod newMethod, Iterable<? extends DexBackedMethod> olds, DiffInfo info) {
        for (DexBackedMethod oldMethod : olds) {
            if (oldMethod.equals(newMethod)) {
                if ((oldMethod.getImplementation() == null) && (newMethod.getImplementation() != null)) {
                    info.addModifiedMethods(newMethod);
                    return;
                }
                if ((oldMethod.getImplementation() != null) && (newMethod.getImplementation() == null)) {
                    info.addModifiedMethods(newMethod);
                    return;
                }
                if ((oldMethod.getImplementation() == null) && (newMethod.getImplementation() == null)) {
                    return;
                }

                if (!oldMethod.getImplementation().equals(newMethod.getImplementation())) {
                    info.addModifiedMethods(newMethod);
                    return;
                }
                return;
            }
        }

        info.addAddedMethods(newMethod);
    }

    public void compareField(DexBackedClassDef newClass, DexBackedClassDef oldClass, DiffInfo info) {
        compareField(newClass.getFields(), oldClass.getFields(), info);
    }

    public void compareField(Iterable<? extends DexBackedField> news,
                             Iterable<? extends DexBackedField> olds, DiffInfo info) {

        for (DexBackedField newField : news) {
            compareField(newField, olds, info);
        }
    }

    public void compareField(DexBackedField newFiled, Iterable<? extends DexBackedField> olds, DiffInfo info) {
        for (DexBackedField oldField : olds) {
            if (oldField.equals(newFiled)) {
                if ((oldField.getInitialValue() == null) && (newFiled.getInitialValue() != null)) {
                    info.addModifiedFields(newFiled);
                    return;
                }
                if ((oldField.getInitialValue() != null) && (newFiled.getInitialValue() == null)) {
                    info.addModifiedFields(newFiled);
                    return;
                }
                if ((oldField.getInitialValue() == null) && (newFiled.getInitialValue() == null)) {
                    return;
                }
                if (oldField.getInitialValue().compareTo(newFiled.getInitialValue()) != 0) {
                    info.addModifiedFields(newFiled);
                    return;
                }
                return;
            }
        }

        info.addAddedFields(newFiled);
    }
}
