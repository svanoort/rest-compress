package org.restcompress.provider;

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
 * Why LZF?
 *   - GZIP gives good compression, but is relatively CPU-expensive
 *   - Within a data center, using GZIP will reduce performance because the CPU time to compress outweighs bandwidth saved
 *   - LZF is a similar LZ77 algorithm that trades some compression for much faster speeds
 *   - LZF has widespread multilanguage support: Java, C, Python, Javascript, Ruby, Perl, and Go
 *   - For list of compatible LZF clients: see https://github.com/ning/compress#interoperability
 *
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ContentEncoding("lzf")
public @interface LZF {
}
