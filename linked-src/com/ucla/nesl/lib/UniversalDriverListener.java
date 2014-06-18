package com.ucla.nesl.lib;

/*
 * Interface that is to be implemented by all the Drivers.
 */
public interface UniversalDriverListener {
	public void setRate(int sType, int rate, int bundleSize);
	public void disconnected();
}
