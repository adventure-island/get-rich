package com.smartj.getrich.quantum;

/**
 * @author JJ Sun
 *
 */
public enum DrawingAlgorithm {
	/**
	 * This algorithm requests a random quantum number at first, scales it to a
	 * value within the target range and then uses it as an index to select the
	 * number in the pool.
	 */
	ShrinkingIndexPool,
	OneTimeIndexSelection
}
