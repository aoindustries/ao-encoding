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

import java.io.IOException;

/**
 * Encodes a URL into an XHTML attribute.  It uses HttpServletRequest.encodeURL to
 * rewrite the URL as needed.
 *
 * @author  AO Industries, Inc.
 */
public class UrlInXhtmlAttributeEncoder extends BufferedEncoder {

	private final EncodingContext context;

	public UrlInXhtmlAttributeEncoder(EncodingContext context) {
		super(128);
		this.context = context;
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
		return MediaType.XHTML_ATTRIBUTE;
	}

	@Override
	protected void writeSuffix(StringBuilder buffer, Appendable out) throws IOException {
		TextInXhtmlAttributeEncoder.encodeTextInXhtmlAttribute(
			context.encodeURL(buffer.toString()),
			out
		);
	}
}
