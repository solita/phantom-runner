package fi.solita.phantomrunner.util;

import java.lang.annotation.Annotation;

public class ClassUtils {

	@SuppressWarnings("unchecked")
	public static <T extends Annotation> T findClassAnnotation(Class<T> annotationClass, Class<?> fromClass, boolean required) {
		for (Annotation a : fromClass.getAnnotations()) {
			if (a.annotationType().equals(annotationClass)) {
				return (T) a;
			}
		}
		
		if (required) {
			throw new IllegalStateException(String.format(
					"Illegal PhantomRunner configuration, no %s annotation found at %s type level", 
					annotationClass.getName(), fromClass.getName()));
		}
		return null;
	}
	
}
