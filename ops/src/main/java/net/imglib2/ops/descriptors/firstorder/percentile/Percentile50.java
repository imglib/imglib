package net.imglib2.ops.descriptors.firstorder.percentile;

import net.imglib2.ops.descriptors.ModuleInput;
import net.imglib2.ops.descriptors.firstorder.impl.SortedValues;

public class Percentile50 extends Percentile 
{
	@ModuleInput
	SortedValues sortedValues;
	
	@Override
	public String name() {
		return "Percentile 50";
	}
	
	@Override
	protected double calculateFeature() {
		
		return this.calculatePercentile(0.5, sortedValues.get());
	}
}