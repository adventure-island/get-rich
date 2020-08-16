package com.smartj.getrich.main;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.smartj.getrich.main.LabManager;

/**
 * @author JJ Sun
 *
 */
@ComponentScan("com.smartj")
@SpringBootApplication
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class SmartJLabApplication {

	@Autowired
	private LabManager myMgr;

	public static void main(String[] args) {
		SpringApplication.run(SmartJLabApplication.class, args);
	}

	@Bean
	public CommandLineRunner runGame() {
		return (args) -> {
			myMgr.runLottoGame();
		};
	}

}