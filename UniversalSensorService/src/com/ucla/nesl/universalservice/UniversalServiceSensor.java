package com.ucla.nesl.universalservice;

import java.util.ArrayList;

public class UniversalServiceSensor {
	public ArrayList<UniversalServiceListener> registeredlisteners;
	
	public UniversalServiceSensor()
	{
		registeredlisteners = new ArrayList<UniversalServiceListener>();
	}
	public void registerListener(UniversalServiceListener mlistener)
	{
		registeredlisteners.add(mlistener);
	}
	
	public void unregisterListner(UniversalServiceListener mliListener)
	{
		
	}
}
