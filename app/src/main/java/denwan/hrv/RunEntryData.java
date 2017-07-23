package denwan.hrv;

import java.io.Serializable;

/**
 * Created by tanne on 03.07.2017.
 */

public class RunEntryData implements Serializable {
    public int intensity = 0;
    public int totalTimeSeconds = 0;
    public float distanceMeters = 0.0f;
    public int maxHR = 0;
    public float avgHR = 0.f;
    public float avgSpeed_ms = 0.0f;
    public float avgCadence = 0.0f;
    public float avgTemperature = 0.0f;
    public int rpe = 0;
}
