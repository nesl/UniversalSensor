package com.ucla.nesl.aidl;

interface IUniversalDriverManager {
	void setRate(int rate);
	void activateSensor(int sType);
	void deactivateSensor(int sType);
}