/*
 * ao-encoding - High performance character encoding.
 * Copyright (C) 2009, 2010, 2011, 2013, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-encoding.
 *
 * ao-encoding is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-encoding is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-encoding.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.encoding;

import static com.aoindustries.encoding.TextInJavaScriptEncoder.encodeTextInJavaScript;
import com.aoindustries.net.UrlUtils;
import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

/**
 * Encodes a URL into a JavaScript string.  It uses HttpServletRequest.encodeURL
 * to rewrite the URL as needed and surrounds it in double quotes.
 *
 * @author  AO Industries, Inc.
 */
public class UrlInJavaScriptEncoder extends BufferedEncoder {

    private final HttpServletResponse response;

	public UrlInJavaScriptEncoder(HttpServletResponse response) {
		super(128);
        this.response = response;
    }

    @Override
    public boolean isValidatingMediaInputType(MediaType inputType) {
        return
            inputType==MediaType.URL
            || inputType==MediaType.TEXT        // No validation required
        ;
    }

    @Override
    public MediaType getValidMediaOutputType() {
        return MediaType.JAVASCRIPT;
    }

    @Override
    public void writePrefixTo(Appendable out) throws IOException {
        out.append('"');
    }

	@Override
    protected void writeSuffix(StringBuilder buffer, Appendable out) throws IOException {
        encodeTextInJavaScript(
            response.encodeURL(
                UrlUtils.encodeUrlPath(
                    buffer.toString(),
					response.getCharacterEncoding()
                )
            ),
            out
        );
        out.append('"');
    }
}
