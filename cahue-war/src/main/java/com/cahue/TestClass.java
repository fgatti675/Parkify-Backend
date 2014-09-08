package com.cahue;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Date;

@Singleton
public class TestClass {

	@Inject
	private Date date;
	
	public TestClass() {
	}
	
	public Date getDate() {
		return date;
	}
}
