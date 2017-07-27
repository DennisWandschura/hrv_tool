package denwan.measurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;

/**
 * Created by tanne on 12.07.2017.
 */

public class HRV implements Serializable {
    public static final String MEASUREMENT_TAG = "MEASUREMENT_DATETIME";
    public static final int MEASUREMENT_TAG_ID = 1337;
    static final int TYPE_ID = 0;

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
            obj.put("sdnn", sdnn);
            obj.put("rmssd", rmssd);
            obj.put("sdsd", sdsd);
            obj.put("nn50", nn50);
            obj.put("nn20", nn20);
            obj.put("pnn50", pnn50);
            obj.put("pnn20", pnn20);
            return obj;
    }

    public void read(ObjectInputStream in) throws IOException
    {
        sdnn = in.readDouble();
        rmssd = in.readDouble();
        sdsd = in.readDouble();
        nn50 = in.readInt();
        nn20 = in.readInt();
        pnn50 = in.readDouble();
        pnn20 = in.readDouble();
    }

    public void write(ObjectOutputStream out) throws IOException
    {
        out.writeInt(TYPE_ID);
        out.writeDouble(sdnn);
        out.writeDouble(rmssd);
        out.writeDouble(sdsd);
        out.writeInt(nn50);
        out.writeInt(nn20);
        out.writeDouble(pnn50);
        out.writeDouble(pnn20);
    }
}
