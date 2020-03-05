/**
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package net.imglib2.interpolation.randomaccess;

import org.junit.Test;

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import net.imglib2.FinalInterval;
import net.imglib2.RealRandomAccessible;
import net.imglib2.position.FunctionRandomAccessible;
import net.imglib2.type.numeric.integer.UnsignedByteType;
import net.imglib2.view.Views;

/**
 *
 *
 * @author Stephan Saalfeld &lt;saalfelds@janelia.hhmi.org&gt;
 */
public class CardinalBSplineInterpolatorTest
{
	@Test
	public final static void main( final String... args)
	{
		final FunctionRandomAccessible<UnsignedByteType> img = new FunctionRandomAccessible<>(
				2,
				( a, b ) -> {
					b.setReal(1);
				},
				UnsignedByteType::new);


		final CardinalBSplineInterpolatorFactory<UnsignedByteType> factory = new CardinalBSplineInterpolatorFactory<>();

		final FinalInterval interval = new FinalInterval( 200, 100 );

		// show original image
		final BdvOptions opts = BdvOptions.options();
		final BdvStackSource< UnsignedByteType > bdv = BdvFunctions.show(img, interval, "img", opts);

		// show interpolated image
		final RealRandomAccessible< UnsignedByteType > imgInterp = Views.interpolate( img, factory );
		BdvFunctions.show( imgInterp, interval, "img interp", opts.addTo( bdv ));
	}

}