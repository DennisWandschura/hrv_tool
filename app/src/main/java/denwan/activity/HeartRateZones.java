package denwan.activity;

import java.io.Serializable;

import android.graphics.Color;

/**
 * Created by tanne on 16.07.2017.
 */

public class HeartRateZones implements Serializable {
    static final int DEFAULT_ZONE_COUNT = 5;

    public class Zone implements Serializable {
        public int fromPercent;
        public int toPercent;
        public String name;
        public int color;

        public Zone(String name, int fromPercent, int toPercent, int color)
        {
            this.fromPercent = fromPercent;
            this.toPercent = toPercent;
            this.name = name;
            this.color = color;
        }
    }

    Zone[] m_zones;
    int m_threshold;

    public HeartRateZones() {
        m_zones = new Zone[DEFAULT_ZONE_COUNT];
        m_threshold = 0;
    }

    public void setThreshold(int threshold) {
        m_threshold = threshold;
    }

    public void setZoneData(int idx, String name, int fromPercent, int toPercent) {
        m_zones[idx].name = name;
        m_zones[idx].fromPercent = fromPercent;
        m_zones[idx].toPercent = toPercent;
    }

    void setDefaultZones()
    {
        m_zones = new Zone[DEFAULT_ZONE_COUNT];
        m_zones[0] = new Zone("Zone1", 70, 80, 0xFF808080);
        m_zones[1] = new Zone("Zone2", 81, 90, 0xFF007FFF);
        m_zones[2] = new Zone("Zone3", 91, 95, Color.GREEN);
        m_zones[3] = new Zone("Zone4", 96, 100, 0xFFFF8000);
        m_zones[4] = new Zone("Zone5", 100, 105, Color.RED);
    }

    public void setDefaults() {
        m_threshold = 180;

        setDefaultZones();
    }

    public Zone getZone(int idx) { return m_zones[idx]; }

    public int getCount() { return m_zones.length; }

}
