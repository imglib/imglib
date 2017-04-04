/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imglib2.view;

import org.junit.Test;

import net.imglib2.RandomAccessible;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.type.numeric.integer.UnsignedByteType;

/**
 * Tests {@link Views}.
 *
 * @author Ellen T Arena
 *
 */
public class ViewsTest
{
	/** Tests {@link Views#permute(net.imglib2.RandomAccessible, int, int)}. */
	@Test( expected = IllegalArgumentException.class )
	public void testPermuteRA()
	{
		final RandomAccessible< UnsignedByteType > img = ArrayImgs.unsignedBytes( 10, 10 );
		Views.permute( img, 4, 5 );
	}

	/**
	 * Tests
	 * {@link Views#permute(net.imglib2.RandomAccessibleInterval, int, int)}.
	 */
	@Test( expected = IllegalArgumentException.class )
	public void testPermuteRAI()
	{
		Views.permute( ArrayImgs.unsignedBytes( 10, 10 ), 4, 5 );
	}

	/**
	 * Tests
	 * {@link Views#rotate(net.imglib2.RandomAccessibleInterval, int, int)}.
	 */
	@Test( expected = IllegalArgumentException.class )
	public void testRotateRAI()
	{
		Views.rotate( ArrayImgs.unsignedBytes( 10, 10 ), 4, 5 );
	}

	/** Tests {@link Views#rotate(net.imglib2.RandomAccessible, int, int)}. */
	@Test( expected = IllegalArgumentException.class )
	public void testRotateRA()
	{
		final RandomAccessible< UnsignedByteType > img = ArrayImgs.unsignedBytes( 10, 10 );
		Views.rotate( img, 4, 5 );
	}
}
