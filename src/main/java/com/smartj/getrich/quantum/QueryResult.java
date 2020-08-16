package com.smartj.getrich.quantum;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResult {
	/*
	 * Sample message:
	 * {"type":"uint8","length":10,"data":[228,20,204,160,21,145,240,215,251,240],"success":true}
	 */
	private String type;
	private int length;
	private List<Integer> data;
	private boolean success;
}
