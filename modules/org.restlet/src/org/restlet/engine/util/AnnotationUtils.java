package org.restlet.engine.util;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Method;
import org.restlet.resource.UniformResource;

/**
 * Utilities to manipulate Restlet annotations.
 * 
 * @author Jerome Louvel
 */
public class AnnotationUtils {

    /**
     * Returns the annotation descriptors for the given resource class.
     * 
     * @param context
     *            The optional parent context used for caching.
     * @param resourceClass
     *            The resource class to introspect.
     * @return The list of annotation descriptors.
     */
    @SuppressWarnings("unchecked")
    public static List<AnnotationInfo> getAnnotationDescriptors(
            Context context, Class<? extends UniformResource> resourceClass) {
        List<AnnotationInfo> result = null;
        String attributeName = "org.restlet.resource.ServerResource.annotations."
                + resourceClass.getCanonicalName();

        if (context != null) {
            result = (List<AnnotationInfo>) context.getAttributes().get(
                    attributeName);
        }

        if (result == null) {
            result = new ArrayList<AnnotationInfo>();

            if (context != null) {
                context.getAttributes().put(attributeName, result);
            }

            for (java.lang.reflect.Method javaMethod : resourceClass
                    .getDeclaredMethods()) {
                for (Annotation annotation : javaMethod.getAnnotations()) {

                    Annotation methodAnnotation = annotation.annotationType()
                            .getAnnotation(org.restlet.engine.Method.class);

                    if (methodAnnotation != null) {
                        Method restletMethod = Method
                                .valueOf(((org.restlet.engine.Method) methodAnnotation)
                                        .value());

                        String toString = annotation.toString();
                        int startIndex = annotation.annotationType()
                                .getCanonicalName().length() + 8;
                        int endIndex = toString.length() - 1;
                        String value = toString.substring(startIndex, endIndex);
                        if ("".equals(value)) {
                            value = null;
                        }

                        // Add the annotation descriptor
                        if (result == null) {
                            result = new ArrayList<AnnotationInfo>();
                        }

                        result.add(new AnnotationInfo(restletMethod,
                                javaMethod, value));
                    }
                }
            }
        }
        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private AnnotationUtils() {
    }

}