package com.smartj.getrich.quantum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class QuantumRandomGenerator
		implements InitializingBean, DisposableBean {
	private static final String REQUEST_URL = "https://qrng.anu.edu.au/API/jsonI.php?length={$count}&type=uint8";
	private static final int RAW_RANGE_MIN = 0;
	private static final int RAW_RANGE_MAX = 255;
	private static final int EXTRA_NUMBERS = 200;

	public QueryResult retrieveNumbers(int count) {
		log.debug("retrieveNumbers - START, sending request with count: {}",
				count);
		RestTemplate restTemplate = new RestTemplate(
				getClientHttpRequestFactory());
		QueryResult r = restTemplate.getForObject(REQUEST_URL,
				QueryResult.class, count);
		log.debug("Result returned: {}", r);
		return r;
	}

	private ClientHttpRequestFactory getClientHttpRequestFactory() {
		int timeout = 60 * 1000;
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
		clientHttpRequestFactory.setConnectTimeout(timeout);
		return clientHttpRequestFactory;
	}

	private void generateFinalSelection(List<RandomRecord> gameNumbers,
			List<Integer> powerballs) {

		log.info("------------------ FINAL SELECTION ---------------------");
		// Simply pair numbers and print them out
		for (int i = 0; i < gameNumbers.size(); i++) {
			log.info("Game[{}]: {}[{}]", i, gameNumbers.get(i),
					powerballs.get(i));
		}
	}

	public List<Integer> requestRandomNumberInRange(int count, int min,
			int max) {
		int totalCount = 128;
		QueryResult r = retrieveNumbers(totalCount);
		if (r == null) {
			log.error("Request failed");
		}

		Validate.isTrue(r.isSuccess());
		Validate.isTrue(r.getLength() == totalCount);

		List<Integer> result = new ArrayList<>();
		// Let's start with a simple loop
		List<Integer> ran = r.getData();
		for (int i = 0; i < count; i++) {
			int number = ran.remove(0);
			int scaled = Util.scale(number, RAW_RANGE_MIN, RAW_RANGE_MAX, min,
					max);
			result.add(scaled);
		}

		log.info("requestRandomNumberInRange - result: {}", result);
		return result;
	}

	/**
	 * The entry point of game playing.
	 * 
	 * @param gameType the type of the game you want to play, see 
	 * 	 {@link com.smartj.getrich.quantum.GameType}
	 * @param rounds the number of rounds you want to play
	 */
	public void playGame(GameType gameType, int rounds) {
		List<RandomRecord> gameNumbers = null;
		switch (gameType.algo) {
		case ShrinkingIndexPool:
			gameNumbers = requestQuantumNumWithRandomIndexSelection(gameType, rounds);
			break;
		case OneTimeIndexSelection:
			gameNumbers = playWithOneTimeIndexSelection(gameType, rounds);
			break;
		default:
			break;
		}
		
		if(gameType == GameType.POWERBALL) {
			// Powerball requires 7 main numbers and one powerball number
			// This returns a list of rows containing the selected game numbers
			// Retrieve powerballs then combine the result
			List<Integer> powerballs = requestRandomNumberInRange(rounds,
					gameType.min, 20);
			generateFinalSelection(gameNumbers, powerballs);
		} else if(gameType == GameType.CN_BINGO_BALL) {
			// CN_BINGO_BALL requires 6 main numbers(red balls) and one number(
			// blue ball)
			// This returns a list of rows containing the selected game numbers
			List<Integer> powerballs = requestRandomNumberInRange(rounds,
					gameType.min, 16);
			generateFinalSelection(gameNumbers, powerballs);
		}
	}
	
	/**
	 * This algorithm requests a list of quantum numbers at first, scales them to 
	 * the values within the target range and then uses them as indices to draw the
	 * balls in the pool.
	 * 
	 * @param gameType the type of the game to play
	 * @param rounds the rounds of game to play
	 */
	private List<RandomRecord> requestQuantumNumWithRandomIndexSelection(
			GameType gameType, int rounds) {

		int numsPerRow = gameType.numBalls;

		log.info(
				"requestQuantumNumWithRandomIndexSelection - Requesting quantum "
				+ "numbers, countPerRow: {}, rounds: {}",
				numsPerRow, rounds);

		// Use bigger count here so we have extra candidates and we can choose a
		// different number if duplication occurs
		int totalCount = numsPerRow * rounds + EXTRA_NUMBERS;
		QueryResult r = retrieveNumbers(totalCount);
		if (r == null) {
			log.error("Request failed");
		}

		Validate.isTrue(r.isSuccess());
		Validate.isTrue(r.getLength() == totalCount);

		List<Integer> rands = r.getData();
		List<RandomRecord> resultGroup = new ArrayList<>();

		/*
		 * How is the random selection determined? 
		 * 1. First we define a sequential 'pool' of numbers containing the 
		 * sequential lotto numbers - (1, 2, 3, ..., 45) 
		 * 2. From the random numbers(which are between 0~255) returned from 
		 * the API 
		 * 	2.1 We pop the first random number: r
		 * 	2.2 Scale 'r' from its original range to the target range (e.g.,
		 * 		1~45): rt 
		 * 	2.3 Use rt as the index to select/draw a number from the sequential
		 * 		pool: selectedNumber = pool[rt] 
		 *  2.4 Remove selected number from pool then push it to another list 
		 *  	which is maintained in a `RandomRecord` object 
		 *  2.5 Repeat 2.1 to 2.4 until the `RandomRecord` object has the
		 * 		expected total number of balls 
		 * 3. Repeat step 3 to finish the specified `rounds` of playing
		 */
		for (int round = 0; round < rounds; round++) {
			Set<Integer> usedRandNums = new HashSet<>();
			// At first, initialize the 'pool', note that this is a pool with
			// sequential numbers
			Set<Integer> sequentialPool = new HashSet<>();
			for (int i = 0; i < gameType.max; i++) {
				sequentialPool.add(i + 1);
			}

			log.debug("Random pool(set) initialized: {}, round: {}",
					sequentialPool, round);

			int iterationCount = 0;
			// Create a record to hold the row of numbers selected from pool
			RandomRecord record = new RandomRecord(numsPerRow);

			log.debug("Selection round[{}] started", round);

			// Start drawing numbers for one round of play
			while (!record.isCompleted()) {
				// Always select and remove the first number
				log.debug("Before remove - size: {}, data: {}", rands.size(),
						rands);
				int number = rands.remove(0);

				// Avoid drawing duplicated numbers
				if (usedRandNums.contains(number)) {
					log.info(
							"Skipping already used number: {}, current used numbers: {}",
							number, usedRandNums);
					continue;
				}
				usedRandNums.add(number);

				log.debug("Random number selected: {}", number);

				// Get the scaled random index, which will be used to select the
				// number in the pool, note that the sequential pool 'shrinks' because once
				// we select the number, it will be removed from the pool, so 
				// the scaling range also shrinks
				int scaledRandomInxInPool = Util.scale(number, RAW_RANGE_MIN,
						RAW_RANGE_MAX, gameType.min,
						gameType.max - iterationCount);

				// Now use the scaled index to select the number, note the pool
				// index is zero-based
				int zeroBasedRandomIndex = scaledRandomInxInPool - 1;

				log.debug("Current pool size: {}", sequentialPool.size());

				Validate.isTrue(
						sequentialPool.size() == gameType.max - iterationCount);
				Validate.inclusiveBetween(0, sequentialPool.size() - 1,
						zeroBasedRandomIndex);

				// Pick a number from the pool using the random index
				int interatedIdx = 0;
				int selectedNumber = -1;
				Iterator<Integer> it = sequentialPool.iterator();
				while (it.hasNext()) {
					selectedNumber = it.next();
					if (interatedIdx == zeroBasedRandomIndex) {
						sequentialPool.remove(selectedNumber);
						break;
					} else {
						selectedNumber = -1;
					}
					interatedIdx++;
				}

				Validate.inclusiveBetween(gameType.min, gameType.max,
						selectedNumber);

				log.debug(
						"Iteration[{}]: Number selected from random sequece: {}, "
						+ "scaled to index value: {}, selected number in pool:{}",
						iterationCount, number, scaledRandomInxInPool,
						selectedNumber);

				// Perform filtering if some of the features are enabled
				if (gameType.avoidLeastProfitableNums
						&& gameType.leastProfitableNums
								.contains(selectedNumber)) {
					log.warn("Ignoring non-profitalbe number: {}",
							selectedNumber);
				} else if (gameType.avoidBlackListedNums
						&& gameType.blackListedNums.contains(selectedNumber)) {
					log.warn("Ignoring blacklisted number: {}", selectedNumber);
				} else {
					boolean added = record.add(selectedNumber);
					if (!added) {
						throw new IllegalStateException(
								"This should not happen.");
					}
				}

				log.debug("Current record data: {}", record);
				iterationCount++;
			}

			resultGroup.add(record);

			log.debug("Selection round[{}] completed, record: {}", round,
					record);
		}

		if (gameType.useRanking) {
			log.debug("Ranking is enabled");
			rankRecords(resultGroup, gameType);
		}

		// Sort in reverse natural order
		Collections.sort(resultGroup, Collections.reverseOrder());

		log.info("Printing final result");
		for (int i = 0; i < resultGroup.size(); i++) {
			RandomRecord row = resultGroup.get(i);
			log.info("Result row[{}]: {}", i, row);
		}

		log.info("requestQuantumNumWithRandomIndexSelection - Done.");
		return resultGroup;
	}

	/**
	 * This algorithm selects balls based on scaled random indices, no sequential
	 * pool is used.
	 * 
	 * @param numsPerRow
	 * @param rounds
	 */
	private List<RandomRecord> playWithOneTimeIndexSelection(GameType gameType,
			int rounds) {

		int numsPerRow = gameType.numBalls;

		log.info("playWithOneTimeIndexSelection - Requesting quantum numbers, "
				+ "countPerRow: {}, rounds: {}", numsPerRow, rounds);

		// Use bigger count here so we have extra candidates and we can choose a
		// different number if duplication occurs
		int totalCount = numsPerRow * rounds + EXTRA_NUMBERS;
		QueryResult r = retrieveNumbers(totalCount);
		if (r == null) {
			log.error("Request failed");
		}

		Validate.isTrue(r.isSuccess());
		Validate.isTrue(r.getLength() == totalCount);

		// Let's start with a simple loop
		List<Integer> ran = r.getData();
		List<RandomRecord> resultGroup = new ArrayList<>();

		for (int round = 0; round < rounds; round++) {
			// Discard already used random numbers
			Set<Integer> usedRandNums = new HashSet<>();

			int iterationCount = 0;

			// Create a record to hold the row of numbers selected from pool
			RandomRecord record = new RandomRecord(numsPerRow);

			log.debug("Selection round[{}] started", round);

			while (!record.isCompleted()) {
				// Always select and remove the first number
				log.debug("Before remove - size: {}, data: {}", ran.size(),
						ran);

				int number = ran.remove(0);
				if (usedRandNums.contains(number)) {
					log.info("Skipping already used number: {}, current used "
							+ "numbers: {}", number, usedRandNums);
					continue;
				}
				usedRandNums.add(number);

				log.debug("Random number selected: {}", number);

				// Get the scaled random number, which will be used as one of
				// the candidate number if it's not duplicated
				int scaledRandomInRange = Util.scale(number, RAW_RANGE_MIN,
						RAW_RANGE_MAX, gameType.min, gameType.max);

				Validate.inclusiveBetween(gameType.min, gameType.max,
						scaledRandomInRange);

				log.debug(
						"Iteration[{}]: Number selected from random sequece: "
								+ "{}, scaled to value: {}",
						iterationCount, number, scaledRandomInRange);

				if (gameType.avoidLeastProfitableNums
						&& gameType.leastProfitableNums
								.contains(scaledRandomInRange)) {
					log.warn("Ignoring non-profitalbe number: {}",
							scaledRandomInRange);
				} else if (gameType.avoidBlackListedNums
						&& gameType.blackListedNums
								.contains(scaledRandomInRange)) {
					log.warn("Ignoring blacklisted number: {}",
							scaledRandomInRange);
				} else {
					boolean added = record.add(scaledRandomInRange);
					if (!added) {
						// Do nothing, try next number
					}
				}

				log.debug("Current record data: {}", record);
				iterationCount++;
			}

			resultGroup.add(record);

			log.debug("Selection round[{}] completed, record: {}", round,
					record);
		}

		if (gameType.useRanking) {
			log.debug("Ranking is enabled");
			rankRecords(resultGroup, gameType);
		}

		// Sort in reverse natural order
		Collections.sort(resultGroup, Collections.reverseOrder());

		log.info("Printing final result");
		for (int i = 0; i < resultGroup.size(); i++) {
			RandomRecord row = resultGroup.get(i);
			log.info("Result row[{}]: {}", i, row);
		}

		log.info("playWithOneTimeIndexSelection - Done.");
		return resultGroup;
	}

	private void rankRecords(List<RandomRecord> resultGroup,
			GameType gameType) {
		for (RandomRecord r : resultGroup) {
			gameType.mostProfitableNums.forEach(item -> {
				if (r.getResults().contains(item)) {
					r.setRank(r.getRank() + 1);
				}
			});
		}
	}

	@PostConstruct
	public void init() {
		log.debug("PostConstruct");
	}

	@PreDestroy
	public void predestroy() {
		log.debug("PreDestroy");
	}

	@Override
	public void destroy() throws Exception {
		log.debug("DisposableBean - destroy");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.debug("InitializingBean - afterPropertiesSet");
	}

}
