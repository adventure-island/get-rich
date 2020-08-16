package com.smartj.getrich.quantum;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import lombok.RequiredArgsConstructor;

/**
 * The enumeration class describing types of the lotto game.
 * 
 * @author JJ Sun
 */
@RequiredArgsConstructor
public enum GameType {
	XLOTTO( 
		// number of balls to draw
		6, 
		// the minimum value of a ball
		1,
		// the maximum value of a ball
		45,
		// most profitable numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(31, 27, 16, 37, 40, 2))),
		// least profitable numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(22, 26, 12, 34, 25, 24))),
		// black listed numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(1, 2))),
		// the algorithm to use to generate the numbers
		DrawingAlgorithm.OneTimeIndexSelection,
		true, //avoidLeastProfitableNums
		true, //avoidBlackListedNums
		true  //useRanking
	),
	OZLOTTO(
		// number of balls to draw
		7,
		// the minimum value of a ball
		1,
		// the maximum value of a ball
		45,
		// most profitable number
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(29, 25, 19, 32, 24, 5, 42))),
		// least profitable numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(39, 33, 17, 11, 10, 41, 43))),
		// black listed numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(1))),
		// the algorithm to use to generate the numbers
		DrawingAlgorithm.ShrinkingIndexPool,
		true, //avoidLeastProfitableNums
		true, //avoidBlackListedNums
		true  //useRanking
	), 
	POWERBALL(
		// number of balls to draw
		7, 
		// the minimum value of a ball
		1,
		// the maximum value of a ball
		35,
		// most profitable number
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(26, 31, 13, 4, 18, 27, 11))),
		// least profitable numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(19, 9, 25, 24, 29, 21, 7))),
		// black listed numbers
		Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(1))),
		// the algorithm to use to generate the numbers
		DrawingAlgorithm.OneTimeIndexSelection,
		false, //avoidLeastProfitableNums
		false, //avoidBlackListedNums
		true  //useRanking
	);
	 
	public final int numBalls;
	public final int min;
	public final int max;
	public final Set<Integer> mostProfitableNums;
	public final Set<Integer> leastProfitableNums;
	public final Set<Integer> blackListedNums;
	public final DrawingAlgorithm algo;
	public final boolean avoidLeastProfitableNums;
	public final boolean avoidBlackListedNums;
	public final boolean useRanking;
}
