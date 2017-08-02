package denwan.hrv;

/**
 * Created by tanne on 29.07.2017.
 */

public class Native {
    static {
        System.loadLibrary("native-lib");
    }

    public static final String MEASUREMENT_IDX = "MEASUREMENT_DATETIME";
    public static final String UPDATE_INDICES = "UPDATE_INDICES";

    public static native void initialize();
    public static native void shutdown();

    public static native void saveData(String file);
    public static native int loadData(String file);

    // returns index or -1 if indices were changed, -2 on error
    public static native int createNewEntry(int year, int month, int day, int hour, int minute, float rr[], boolean isFirstOfDay);
    public static native void updateIndices();

    public static native int getEntryCount();
    public static native int getFirstOfToday(int year, int month, int day);

    public static native DateTime getDateTime(int idx);
    public static native int getIndex(int year, int month, int day, int hour, int minute);

    // get average rmssd [start, end]
    public static native float getAverageRmssd(int start_year, int start_month, int start_day, int end_year, int end_month, int end_day);

    public static native int isFirstOfDay(int idx);
    public static native float getAvgRR(int idx);
    public static native float getSDNN(int idx);
    public static native float getRMSSD(int idx);
    public static native float getSDSD(int idx);
    public static native float getPNN50(int idx);
    public static native float getPNN20(int idx);
    public static native float getVLF(int idx);
    public static native float getLF(int idx);
    public static native float getHF(int idx);

    public static native int getSleep(int idx);
    public static native void setSleep(int idx, int value);

    public static native int getMental(int idx);
    public static native void setMental(int idx, int value);

    public static native int getPhysical(int idx);
    public static native void setPhysical(int idx, int value);
}
