package net.imglib2.ops.descriptors.haralick.helpers;

import net.imglib2.ops.data.CooccurrenceMatrix;
import net.imglib2.ops.descriptors.AbstractDescriptorModule;
import net.imglib2.ops.descriptors.ModuleInput;
import net.imglib2.ops.descriptors.haralick.CoocccurrenceMatrix;

public class CoocHXY extends AbstractDescriptorModule
{
	private static final double EPSILON = 0.00000001f;

	@ModuleInput
	CoocParameter param;

	@ModuleInput
	CoocccurrenceMatrix cooc;

	@ModuleInput
	CoocPX coocPX;

	@ModuleInput
	CoocPY coocPY;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected double[] recompute()
	{
		double hx = 0.0d;
		double hy = 0.0d;
		double hxy1 = 0.0d;
		double hxy2 = 0.0d;

		final CooccurrenceMatrix matrix = cooc.get();
		final int nrGrayLevels = param.nrGrayLevels;
		final double[] px = coocPX.get();
		final double[] py = coocPY.get();

		for ( int i = 0; i < px.length; i++ )
		{
			hx += px[ i ] * Math.log( px[ i ] + EPSILON );
		}
		hx = -hx;

		for ( int j = 0; j < py.length; j++ )
		{
			hy += py[ j ] * Math.log( py[ j ] + EPSILON );
		}
		hy = -hy;
		for ( int i = 0; i < nrGrayLevels; i++ )
		{
			for ( int j = 0; j < nrGrayLevels; j++ )
			{
				hxy1 += matrix.getValueAt( i, j ) * Math.log( px[ i ] * py[ j ] + EPSILON );
				hxy2 += px[ i ] * py[ j ] * Math.log( px[ i ] * py[ j ] + EPSILON );
			}
		}
		hxy1 = -hxy1;
		hxy2 = -hxy2;

		return new double[] { hx, hy, hxy1, hxy2 };
	}

	@Override
	public String name()
	{
		return "CooHXY";
	}

}