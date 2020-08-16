package com.smartj.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.smartj.getrich.quantum.Util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UtilTest {
	private static final int ANU_RANGE_MIN = 0;
	private static final int ANU_RANGE_MAX = 255;
	private static final int TARGET_RANGE_MIN = 1;
	private static final int TARGET_RANGE_MAX = 45;
	
	@Test
	public void test() {
		int value = 0;
		int scaled = Util.scale(value, ANU_RANGE_MIN, ANU_RANGE_MAX, TARGET_RANGE_MIN, TARGET_RANGE_MAX);
		log.debug("Scaled number: {} -> {}", value, scaled);
		assertEquals(TARGET_RANGE_MIN, scaled);
		
		value = 255;
		scaled = Util.scale(value, ANU_RANGE_MIN, ANU_RANGE_MAX, TARGET_RANGE_MIN, TARGET_RANGE_MAX);
		log.debug("Scaled number: {} -> {}", value, scaled);
		assertEquals(TARGET_RANGE_MAX, scaled);
	}

}
