package denwan.activity;

import java.util.ArrayList;

/**
 * Created by tanne on 26.07.2017.
 */

public class BikeActivity extends Activity {
    class Lap
    {

    }

    int strava_id = -1;
    //meters
    public float distance = 0.f;
    //meters per second
    public float avg_speed = 0.f;
    //meters per second
    public float max_speed = 0.f;

    public float average_cadence = 0;
    public float max_cadence = 0;

    public float average_power = 0;
    public float max_power = 0;

    public ArrayList<Lap> laps = new ArrayList<Lap>();
}
