package denwan.measurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by tanne on 12.07.2017.
 */

public class HRV implements Serializable {
    public static final String MEASUREMENT_TAG = "MEASUREMENT_DATETIME";
    public static final int MEASUREMENT_TAG_ID = 1337;
    public static final int TYPE_ID = 0;

    public double sdnn; // standard deviation of RR intervals
    public double rmssd; // root mean square of successive differences
    public double sdsd; // standard deviation of successive differences
    public int nn50; // number of successive pairs with more than 50ms difference
    public int nn20; // number of successive pairs with more than 20ms difference
    public double pnn50; // nn50 divided by count
    public double pnn20 ; // nn20 divided by count
   // public float values[]; // RR interval values

    public HRV()
    {
        sdnn = 0; // standard deviation of RR intervals
        rmssd = 0; // root mean square of successive differences
        sdsd = 0; // standard deviation of successive differences
        nn50 = 0; // number of successive pairs with more than 50ms difference
        nn20 = 0; // number of successive pairs with more than 20ms difference
        pnn50 = 0; // nn50 divided by count
        pnn20 =  0; // nn20 divided by count
       // values = null; // RR interval values
    }

    public HRV(HRV other)
    {
        sdnn = other.sdnn;
        rmssd = other.rmssd;
        sdsd = other.sdsd;
        nn50 = other.nn50;
        nn20 = other.nn20;
        pnn50 = other.pnn50;
        pnn20 = other.pnn20;
       // values = other.values;
    }

    public HRV(JSONObject obj) throws JSONException {
        sdnn = obj.getDouble("sdnn");
        rmssd = obj.getDouble("rmssd");
        sdsd = obj.getDouble("sdsd");
        nn50 = obj.getInt("nn50");
        nn20 = obj.getInt("nn20");
        pnn50 = obj.getDouble("pnn50");
        pnn20 = obj.getDouble("pnn20");
    }

    public JSONObject toJSON() throws JSONException
    {
            JSONObject obj = new JSONObject();
            obj.put("type", TYPE_ID);
            obj.put("sdnn", (double)sdnn);
            obj.put("rmssd", (double)rmssd);
            obj.put("sdsd", (double)sdsd);
            obj.put("nn50", nn50);
            obj.put("nn20", nn20);
            obj.put("pnn50", (double)pnn50);
            obj.put("pnn20", (double)pnn20);
            return obj;
    }
}
