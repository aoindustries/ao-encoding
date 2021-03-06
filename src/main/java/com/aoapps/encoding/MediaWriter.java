/*
 * ao-encoding - High performance streaming character encoding.
 * Copyright (C) 2013, 2015, 2016, 2019, 2020, 2021  AO Industries, Inc.
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

import com.aoapps.hodgepodge.i18n.MarkupCoercion;
import com.aoapps.lang.NullArgumentException;
import com.aoapps.lang.Throwables;
import com.aoapps.lang.io.EncoderWriter;
import com.aoapps.lang.io.Writable;
import com.aoapps.lang.io.function.IOSupplierE;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Optional;

/**
 * Streaming versions of media encoders.
 * 
 * @see  MediaEncoder
 *
 * @author  AO Industries, Inc.
 */
public class MediaWriter extends EncoderWriter implements ValidMediaFilter, TextWriter<MediaWriter> {

	private final EncodingContext encodingContext;
	private final MediaEncoder encoder;

	protected MediaWriter textWriter;

	public MediaWriter(EncodingContext encodingContext, MediaEncoder encoder, Writer out) {
		super(encoder, out);
		this.encodingContext = NullArgumentException.checkNotNull(encodingContext, "encodingContext");
		this.encoder = encoder;
	}

	public EncodingContext getEncodingContext() {
		return encodingContext;
	}

	@Override
	public MediaEncoder getEncoder() {
		return encoder;
	}

	protected MediaWriter getTextWriter() throws UnsupportedEncodingException {
		if(textWriter == null) {
			MediaEncoder textEncoder = MediaEncoder.getInstance(encodingContext, MediaType.TEXT, encoder.getValidMediaInputType());
			textWriter = (textEncoder == null) ? this : new MediaWriter(encodingContext, textEncoder, this);
		}
		return textWriter;
	}

	@Override
	public MediaType getValidMediaInputType() {
		return encoder.getValidMediaInputType();
	}

	@Override
	public boolean isValidatingMediaInputType(MediaType inputType) {
		return encoder.isValidatingMediaInputType(inputType);
	}

	@Override
	public boolean canSkipValidation(MediaType inputType) {
		return encoder.canSkipValidation(inputType);
	}

	@Override
	public MediaType getValidMediaOutputType() {
		return encoder.getValidMediaOutputType();
	}

	@Override
	public MediaWriter append(char c) throws IOException {
		super.append(c);
		return this;
	}

	@Override
	public MediaWriter append(CharSequence csq) throws IOException {
		super.append(csq);
		return this;
	}

	@Override
	public MediaWriter append(CharSequence csq, int start, int end) throws IOException {
		super.append(csq, start, end);
		return this;
	}

	/**
	 * Is indenting enabled?
	 */
	// Matches AnyDocument.indent
	private boolean indent;

	/**
	 * Current indentation level.
	 */
	// Matches AnyDocument.depth
	private int depth;

	// Matches AnyDocument.nl()
	@Override
	public MediaWriter nl() throws IOException {
		out.append(NL);
		return this;
	}

	// Matches AnyDocument.nli()
	@Override
	public MediaWriter nli() throws IOException {
		return nli(0);
	}

	// Matches AnyDocument.nli(int)
	@Override
	public MediaWriter nli(int depthOffset) throws IOException {
		if(getIndent()) {
			WriterUtil.nli(out, getDepth() + depthOffset);
		} else {
			out.append(NL);
		}
		return this;
	}

	// Matches AnyDocument.indent()
	@Override
	public MediaWriter indent() throws IOException {
		return indent(0);
	}

	// Matches AnyDocument.indent(int)
	@Override
	public MediaWriter indent(int depthOffset) throws IOException {
		if(getIndent()) {
			WriterUtil.indent(out, getDepth() + depthOffset);
		}
		return this;
	}

	// Matches AnyDocument.getIndent()
	@Override
	public boolean getIndent() {
		return indent;
	}

	// Matches AnyDocument.setIndent(int)
	@Override
	public MediaWriter setIndent(boolean indent) {
		this.indent = indent;
		return this;
	}

	// Matches AnyDocument.getDepth()
	@Override
	public int getDepth() {
		return depth;
	}

	// Matches AnyDocument.setDepth(int)
	@Override
	public MediaWriter setDepth(int depth) {
		if(depth < 0) throw new IllegalArgumentException("depth < 0: " + depth);
		this.depth = depth;
		return this;
	}

	// Matches AnyDocument.incDepth()
	@Override
	public MediaWriter incDepth() {
		if(getIndent()) {
			int d = ++depth;
			if(d < 0) depth = Integer.MAX_VALUE;
		}
		assert depth >= 0;
		return this;
	}

	// Matches AnyDocument.decDepth()
	@Override
	public MediaWriter decDepth() {
		if(getIndent()) {
			int d = --depth;
			if(d < 0) depth = 0;
		}
		assert depth >= 0;
		return this;
	}

	// Matches AnyDocument.sp()
	@Override
	public MediaWriter sp() throws IOException {
		out.append(SPACE);
		return this;
	}

	// Matches AnyDocument.sp(int)
	@Override
	public MediaWriter sp(int count) throws IOException {
		WriterUtil.sp(out, count);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter nbsp() throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		tw.append(NBSP);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter nbsp(int count) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		WriterUtil.nbsp(tw, count);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text(char ch) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		tw.append(ch);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	// TODO: codePoint?

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text(char[] cbuf) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		if(cbuf != null) tw.write(cbuf);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text(char[] cbuf, int offset, int len) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		if(cbuf != null) tw.write(cbuf, offset, len);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text(CharSequence csq) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		if(csq != null) tw.append(csq);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text(CharSequence csq, int start, int end) throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		if(csq != null) tw.append(csq, start, end);
		if(tw != this) textWriter.encoder.writeSuffixTo(this);
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * If the string is translated, comments will be added giving the
	 * translation lookup id to aid in translation of server-translated values.
	 * </p>
	 */
	@Override
	@SuppressWarnings("UseSpecificCatch")
	public MediaWriter text(Object text) throws IOException {
		// Support Optional
		while(text instanceof Optional) {
			text = ((Optional<?>)text).orElse(null);
		}
		while(text instanceof IOSupplierE<?, ?>) {
			try {
				text = ((IOSupplierE<?, ?>)text).get();
			} catch(Throwable t) {
				throw Throwables.wrap(t, IOException.class, IOException::new);
			}
		}
		if(text instanceof char[]) {
			return text((char[])text);
		}
		if(text instanceof CharSequence) {
			return text((CharSequence)text);
		}
		if(text instanceof Writable) {
			Writable writable = (Writable)text;
			if(writable.isFastToString()) {
				return text(writable.toString());
			}
		}
		if(text instanceof MediaWritable) {
			try {
				return text((MediaWritable<?>)text);
			} catch(Throwable t) {
				throw Throwables.wrap(t, IOException.class, IOException::new);
			}
		}
		// Allow text markup from translations
		MediaWriter tw = getTextWriter();
		if(tw == this) {
			// Already in a textual context
			MarkupCoercion.write(
				text,
				MediaType.TEXT.getMarkupType(),
				false, // Should this be true?
				encoder,
				false,
				out
			);
		} else {
			// Text within a non-textual context
			MarkupCoercion.write(
				text,
				encoder.getValidMediaInputType().getMarkupType(),
				false,
				tw.encoder,
				true,
				this
			);
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * If the string is translated, comments will be added giving the
	 * translation lookup id to aid in translation of server-translated values.
	 * </p>
	 *
	 * @param  <Ex>  An arbitrary exception type that may be thrown
	 */
	@Override
	public <Ex extends Throwable> MediaWriter text(IOSupplierE<?, Ex> text) throws IOException, Ex {
		return text((text == null) ? null : text.get());
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 *
	 * @param  <Ex>  An arbitrary exception type that may be thrown
	 */
	@Override
	public <Ex extends Throwable> MediaWriter text(MediaWritable<Ex> text) throws IOException, Ex {
		try (MediaWriter tw = text()) {
			if(text != null) {
				text.writeTo(tw);
			}
		}
		return this;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Adds {@linkplain MediaEncoder#writePrefixTo(java.lang.Appendable) prefixes}
	 * and {@linkplain MediaEncoder#writeSuffixTo(java.lang.Appendable) suffixes} by media type, such as {@code "…"}.
	 * </p>
	 * <p>
	 * Does not perform any translation markups.
	 * </p>
	 */
	@Override
	public MediaWriter text() throws IOException {
		MediaWriter tw = getTextWriter();
		if(tw != this) textWriter.encoder.writePrefixTo(this);
		return new MediaWriter(
			tw.encodingContext,
			tw.encoder,
			tw.out
		) {
			@Override
			public void close() throws IOException {
				if(tw != this) textWriter.encoder.writeSuffixTo(this);
			}
		};
	}

	// TODO: A set of per-type methods, like xml(), script(), style(), ...

	// TODO: A set of out() methods that take MediaType and value

	// TODO: comments

}
