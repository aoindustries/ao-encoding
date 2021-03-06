/*
 * ao-encoding - High performance streaming character encoding.
 * Copyright (C) 2009, 2010, 2011, 2013, 2015, 2016, 2019, 2020, 2021  AO Industries, Inc.
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
package com.aoapps.encoding;

import java.io.IOException;

/**
 * Encodes a URL into a JavaScript string.  It uses HttpServletRequest.encodeURL
 * to rewrite the URL as needed and surrounds it in double quotes.
 *
 * @author  AO Industries, Inc.
 */
public class UrlInJavaScriptEncoder extends BufferedEncoder {

	private final MediaType outputType;
	private final EncodingContext encodingContext;

	UrlInJavaScriptEncoder(MediaType outputType, EncodingContext encodingContext) {
		super(128);
		if(
			outputType != MediaType.JAVASCRIPT
			&& outputType != MediaType.JSON
			&& outputType != MediaType.LD_JSON
		) {
			throw new IllegalArgumentException("Unsupported output type: " + outputType);
		}
		this.outputType = outputType;
		this.encodingContext = encodingContext;
	}

	@Override
	public MediaType getValidMediaInputType() {
		return MediaType.URL;
	}

	@Override
	public boolean isValidatingMediaInputType(MediaType inputType) {
		return
			inputType==MediaType.URL
			|| inputType==MediaType.TEXT        // No validation required
		;
	}

	@Override
	public boolean canSkipValidation(MediaType inputType) {
		return inputType == MediaType.URL;
	}

	@Override
	public MediaType getValidMediaOutputType() {
		return outputType;
	}

	@Override
	public void writePrefixTo(Appendable out) throws IOException {
		super.writePrefixTo(out);
		out.append('"');
	}

	@Override
	protected void writeSuffix(StringBuilder buffer, Appendable out) throws IOException {
		String url = buffer.toString();
		String encoded = (encodingContext == null) ? url : encodingContext.encodeURL(url);
		UrlValidator.checkCharacters(encoded, 0, encoded.length());
		TextInJavaScriptEncoder.encodeTextInJavaScript(encoded, out);
		out.append('"');
	}
}
