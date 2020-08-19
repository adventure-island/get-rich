package com.smartj.getrich.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.smartj.getrich.quantum.GameType;
import com.smartj.getrich.quantum.QuantumRandomGenerator;

/**
 * The entry point of the application.
 * 
 * @author JJ Sun
 *
 */
@Service("myMgr")
public class LabManager {
	@Autowired
	private QuantumRandomGenerator randomGenerator;

	/**
	 * The entry point of the application
	 */
	public void runLottoGame() {
		int rounds = 50;
		// Specify the game type here
		randomGenerator.playGame(GameType.CN_BINGO_BALL, rounds);
	}

}
