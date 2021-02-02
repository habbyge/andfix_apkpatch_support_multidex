package com.euler.patch.annotation;

import java.util.HashSet;
import java.util.Set;
import org.jf.dexlib2.AnnotationVisibility;
import org.jf.dexlib2.base.BaseAnnotation;
import org.jf.dexlib2.base.BaseAnnotationElement;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.EncodedValue;
import org.jf.dexlib2.immutable.value.ImmutableStringEncodedValue;

import javax.annotation.Nonnull;

public class MethodReplaceAnnotaion extends BaseAnnotation {
    private static final String ANNOTATION = "Lcom/habbyge/iwatch/patch/FixMethodAnno;";
    private final Set<BaseAnnotationElement> mElements = new HashSet<BaseAnnotationElement>();

    public int getVisibility() {
        return AnnotationVisibility.getVisibility("runtime");
    }

    @Nonnull
    public String getType() {
        return "Lcom/habbyge/iwatch/patch/FixMethodAnno;";
    }

    public MethodReplaceAnnotaion(final String clazz, final String method) {
        BaseAnnotationElement clazzElement = new BaseAnnotationElement() {
            public EncodedValue getValue() {
                String name = clazz.substring(1, clazz.length() - 1).replace('/', '.');
                return new ImmutableStringEncodedValue(name);
            }

            public String getName() {
                return "_class";
            }
        };
        this.mElements.add(clazzElement);
        BaseAnnotationElement methodElement = new BaseAnnotationElement() {
            public EncodedValue getValue() {
                return new ImmutableStringEncodedValue(method);
            }

            public String getName() {
                return "method";
            }
        };
        this.mElements.add(methodElement);
    }

    @Nonnull
    public Set<? extends AnnotationElement> getElements() {
        return this.mElements;
    }
}
