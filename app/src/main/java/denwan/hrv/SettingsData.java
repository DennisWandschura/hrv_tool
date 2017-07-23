package denwan.hrv;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by tanne on 18.06.2017.
 */

public class SettingsData {
    public static final String PREFS_NAME = "HrvPrefsFile";

    static final String ANT_DEVICE_NUMBER = "antDeviceNumber";
    static final String EXPORT_RR_VALUES = "exportRrValues";
    static final String EXPORT_MEASUREMENT_SECONDS = "exportMeasurementSeconds";
    static final String STRAVA_CODE = "stravaCode";
    static final String STRAVA_QUERY_TIME = "stravaQueryTime";

    public boolean exportRrValues = true;
    public int antDeviceNumber = 0;
    public float measurementSeconds = 1.0f;
    public String stravaCode = null;
    public int stravaLastQueryTime = 0;

    public void load(Activity activity)
    {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        antDeviceNumber = settings.getInt(ANT_DEVICE_NUMBER, 0);
        exportRrValues = settings.getBoolean(EXPORT_RR_VALUES, true);
        measurementSeconds = settings.getFloat(EXPORT_MEASUREMENT_SECONDS, 1.0f);
        stravaCode = settings.getString(STRAVA_CODE, "0");
        stravaLastQueryTime = settings.getInt(STRAVA_QUERY_TIME, 0);
    }

    public void save(Activity activity)
    {
        SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(ANT_DEVICE_NUMBER, antDeviceNumber);
        editor.putBoolean(EXPORT_RR_VALUES, exportRrValues);
        editor.putFloat(EXPORT_MEASUREMENT_SECONDS, measurementSeconds);
        editor.putString(STRAVA_CODE, stravaCode);
        editor.putInt(STRAVA_QUERY_TIME, stravaLastQueryTime);

        editor.commit();
    }
}
