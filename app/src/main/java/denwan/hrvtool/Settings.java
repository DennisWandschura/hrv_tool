package denwan.hrvtool;

import android.app.Activity;
import android.content.SharedPreferences;

/**
 * Created by tanne on 12.07.2017.
 */

public class Settings {
    public static class Data
    {
        public static final String PREFS_NAME = "HrvPrefsFile";

        static final String ANT_DEVICE_NUMBER = "antDeviceNumber";
        static final String EXPORT_MEASUREMENT_MINUTES = "exportMeasurementMinutes";

        public int antDeviceNumber = 0;
        public float measurementMinutes = 2.0f;
        public denwan.activity.HeartRateZones heartRateZones = null;

        public void load(Activity activity)
        {
            SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
            antDeviceNumber = settings.getInt(ANT_DEVICE_NUMBER, 0);
            measurementMinutes = settings.getFloat(EXPORT_MEASUREMENT_MINUTES, 2.0f);
            //measurementMinutes = 0.5f;
        }

        public void save(Activity activity)
        {
            SharedPreferences settings = activity.getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putInt(ANT_DEVICE_NUMBER, antDeviceNumber);
            editor.putFloat(EXPORT_MEASUREMENT_MINUTES, measurementMinutes);

            editor.commit();
        }
    }

    public static Data DATA = new Data();

    public static void load(Activity activity)
    {
        DATA.load(activity);
    }

    public static void save(Activity activity)
    {
        DATA.save(activity);
    }
}
