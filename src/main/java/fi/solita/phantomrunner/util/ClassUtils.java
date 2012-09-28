/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Solita Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
