package com.restcompress.provider;

import org.jboss.resteasy.annotations.ContentEncoding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to allow LZF compression of REST responses
 *
 * Behaves *exactly* the same as the @GZIP annotation:  if request has header "Accept-Encoding: lzf",
 *  they'll get LZF-compressed responses. Similarly, the server can accept LZF encoding POST bodies.
 *
 * Warning: for now, do not combine with the GZIP annotation
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ContentEncoding("lzf")
public @interface LZF {
}
