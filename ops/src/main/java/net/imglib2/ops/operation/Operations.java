package net.imglib2.ops.operation;

import net.imglib2.IterableInterval;
import net.imglib2.ops.img.UnaryObjectFactory;
import net.imglib2.ops.img.UnaryOperationAssignment;
import net.imglib2.ops.img.UnaryOperationBridge;
import net.imglib2.ops.img.UnaryOperationWrapper;

/**
 * Utility methods to concatenate, wrap, map and compute mainly
 * {@link UnaryOutputOperation}s.
 * 
 */
public final class Operations
{

	private Operations()
	{
		// utility class
	}

	/*
	 * General Joiner
	 */
	/**
	 * Concatenates two operations where the input and output types of the first
	 * operation has a different input type.
	 * 
	 * @param op1
	 *            operation 1 with a different input type
	 * @param op2
	 *            operation 2 with the same input and output type (the same as
	 *            operation 1's output type)
	 * @return a new unary operation
	 */
	@SuppressWarnings( "unchecked" )
	public static < A, B > UnaryOutputOperation< A, B > joinLeft( UnaryOutputOperation< A, B > op1, UnaryOutputOperation< B, B > op2 )
	{
		return new LeftJoinedUnaryOperation< A, B >( op1, concat( op2 ) );
	}

	/**
	 * Concatenates two operations where the input and output types of the
	 * second operation has a different input type.
	 * 
	 * @param op1
	 *            operation 1 with the same input and output type (the same as
	 *            operation 2's input type)
	 * @param op2
	 *            operation 2 with a different output type
	 * @return a new unary operation
	 */
	@SuppressWarnings( "unchecked" )
	public static < A, B > UnaryOutputOperation< A, B > joinRight( UnaryOutputOperation< A, A > op1, UnaryOutputOperation< A, B > op2 )
	{
		return new RightJoinedUnaryOperation< A, B >( concat( op1 ), op2 );
	}

	/**
	 * Concatenates two operations where only the output type of the first
	 * operation matches the input type of the second operation.
	 * 
	 * @param op1
	 * @param op2
	 * @return the concatenation as a new operation
	 */
	public static < A, B, C > UnaryOperationBridge< A, B, C > bridge( UnaryOutputOperation< A, B > op1, UnaryOutputOperation< B, C > op2 )
	{
		return new UnaryOperationBridge< A, B, C >( op1, op2 );
	}

	/**
	 * Concatenates two operations where all inputs and outputs are of the same
	 * type.
	 * 
	 * @param op1
	 * @param op2
	 * @return concatenated operation
	 */
	@SuppressWarnings( "unchecked" )
	public static < A > UnaryOutputOperation< A, A > concat( UnaryOutputOperation< A, A > op1, UnaryOutputOperation< A, A > op2 )
	{
		return concat( new UnaryOutputOperation[] { op1, op2 } );
	}

	public static < A > PipedUnaryOperation< A > concat( UnaryOutputOperation< A, A >... ops )
	{
		return new PipedUnaryOperation< A >( ops );
	}

	/**
	 * Wraps an {@link UnaryOperation} as an {@link UnaryOutputOperation} (such
	 * that methods like {@link #concat(UnaryOutputOperation...)} etc. can be
	 * used)
	 */
	public static < A, B > UnaryOutputOperation< A, B > wrap( final UnaryOperation< A, B > op, final UnaryObjectFactory< A, B > fac )
	{
		return new UnaryOperationWrapper< A, B >( op, fac );
	}

	/**
	 * Returns a new operation which applies the given operation to each single
	 * entry of the {@link IterableInterval}.
	 */
	public static < A, B > UnaryOperation< IterableInterval< A >, IterableInterval< B >> map( UnaryOperation< A, B > op )
	{
		return new UnaryOperationAssignment< A, B >( op );
	}

	/**
	 * Computes all given operations in a sequence.
	 */
	public static < B > B compute( B input, B output, UnaryOutputOperation< B, B >[] ops )
	{

		if ( ops.length == 1 )
		{
			return ops[ 0 ].compute( input, output );
		}
		else
		{

			@SuppressWarnings( "unchecked" )
			UnaryOutputOperation< B, B >[] follower = new UnaryOutputOperation[ ops.length - 1 ];
			System.arraycopy( ops, 1, follower, 0, follower.length );

			return compute( input, output, ops[ 0 ], concat( follower ) );
		}
	}

	public static < B > B compute( B input, B output, PipedUnaryOperation< B > op1, PipedUnaryOperation< B > op2 )
	{

		PipedUnaryOperation< B > unpack = concat( op2.ops() );
		unpack.append( op2.ops() );

		return unpack.compute( input, output );
	}

	public static < A, B > B compute( A input, B output, UnaryOutputOperation< A, B > op1, UnaryOutputOperation< B, B > op2 )
	{
		return op2.compute( op1.compute( input, op1.bufferFactory().instantiate( input ) ), output );
	}

	public static < A, B > B compute( A input, B output, UnaryOutputOperation< A, B > op1, PipedUnaryOperation< B > op2 )
	{

		UnaryOutputOperation< B, B >[] unpack = op2.ops();

		B buffer = op1.bufferFactory().instantiate( input );

		B tmpOutput = output;
		B tmpInput = buffer;
		B tmp;

		if ( unpack.length % 2 == 1 )
		{
			tmpOutput = buffer;
			tmpInput = output;
		}

		op1.compute( input, tmpOutput );

		for ( int i = 0; i < unpack.length; i++ )
		{
			tmp = tmpInput;
			tmpInput = tmpOutput;
			tmpOutput = tmp;
			unpack[ i ].compute( tmpInput, tmpOutput );
		}

		return output;
	}

	public static < A, B > B compute( UnaryOutputOperation< A, B > op, A in )
	{
		return op.compute( in, op.bufferFactory().instantiate( in ) );
	}

	public static < A, B, C > C compute( BinaryOutputOperation< A, B, C > op, A in1, B in2 )
	{
		return op.compute( in1, in2, op.bufferFactory().instantiate( in1, in2 ) );
	}

	// /////////////////////// Iterators ///////////////////////////////
	/*
	 * Iterative Operation
	 */
	public static < A > PipedUnaryOperation< A > iterate( UnaryOutputOperation< A, A > op, int numIterations )
	{

		@SuppressWarnings( "unchecked" )
		UnaryOutputOperation< A, A >[] ops = new UnaryOutputOperation[ numIterations ];

		for ( int i = 0; i < numIterations; i++ )
			ops[ i ] = op;

		return concat( ops );
	}

}