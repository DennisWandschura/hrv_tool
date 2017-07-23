package denwan.activity;

/**
 * Created by tanne on 17.07.2017.
 */

public class Data {

    static final String FILENAME = "activity.data";

    private HeartRateZones m_zones;

    public Data()
    {
        m_zones = new HeartRateZones();
    }

    public HeartRateZones getZones() { return m_zones; }


}
