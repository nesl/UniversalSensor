package com.ucla.nesl.lib;

public interface UniversalEventListener {
    /**                                                                                  
     * Called when sensor values have changed.                                           
     * <p>See {@link android.hardware.SensorManager SensorManager}                       
     * for details on possible sensor types.                                             
     * <p>See also {@link android.hardware.SensorEvent SensorEvent}.                     
     * 
     * <p><b>NOTE:</b> The application doesn't own the                                   
     * {@link android.hardware.SensorEvent event}                                        
     * object passed as a parameter and therefore cannot hold on to it.                  
     * The object may be part of an internal pool and may be reused by                   
     * the framework.
     *  
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.                
     */                                                                                  
    public void onSensorChanged(UniversalSensorEvent event);                                      
    
    /**
     * Called when the accuracy of a sensor has changed.
     * <p>See {@link android.hardware.SensorManager SensorManager}                       
     * for details.
     *
     * @param accuracy The new accuracy of this sensor
     */                                                                                  
    public void onAccuracyChanged(UniversalSensor sensor, int accuracy);
}
