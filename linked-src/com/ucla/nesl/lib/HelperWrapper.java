package com.ucla.nesl.lib;

import com.ucla.nesl.aidl.IUniversalSensorManager;

import android.os.Bundle;

/*
 * This is a useful class for sending objects from one threads
 * to another via Handler.sendMessage. sendMessage takes message
 * as its argument which is usually created by using obtainMessage.
 * ObtainMessage takes only one Object argument and usually we
 * use bundle. But the problem arises as bundle cannot store
 * arbitrary references to class objects. Thus we wrap bundle
 * and the new class object in this class and pass its object.
 */
public class HelperWrapper {
	public Bundle mBundle;
	public IUniversalSensorManager mListener;
	public HelperWrapper(IUniversalSensorManager mListener, Bundle mBundle)
	{
		this.mBundle = mBundle;
		this.mListener = mListener;
	}
}
