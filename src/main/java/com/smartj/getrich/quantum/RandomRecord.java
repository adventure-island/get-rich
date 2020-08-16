package com.smartj.getrich.quantum;

import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is used to contain and manage the selected numbers/balls for 
 * a single round of game playing.
 * 
 * @author JJ Sun
 */
@Data
@Slf4j
public class RandomRecord implements Comparable<RandomRecord> {
	private final int count;
	private final SortedSet<Integer> results;
	private int rank;

	public RandomRecord(int count) {
		Validate.isTrue(count > 0, "the value of count is invalid");
		this.count = count;
		results = new TreeSet<>();
	}

	/**
	 * Adds a number to the record.
	 * 
	 * @param number the selected number/ball.
	 */
	public boolean add(int number) {
		if (results.contains(number)) {
			log.error("Duplicated number encountered: {}, current set: {}",
					number, results);
			return false;
		}

		return results.add(number);
	}

	/**
	 * Checks if this round of playing is completed.
	 * 
	 * @return true if the specified number of balls have been drawn, false 
	 * otherwise.
	 */
	public boolean isCompleted() {
		return results.size() == count;
	}

	@Override
	public int compareTo(RandomRecord arg0) {
		return this.getRank() - arg0.getRank();
	}
}
