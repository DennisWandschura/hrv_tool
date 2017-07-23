package denwan.measurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by tanne on 12.07.2017.
 */

public class FirstOfDay extends HRV implements Serializable {
    public static final int TYPE_ID = 1;

    public int sleepQuality;
    public int mentalHealth;
    public int physicalHealth;

    public FirstOfDay(HRV hrv)
    {
        super(hrv);
        sleepQuality = 0;
        mentalHealth = 0;
        physicalHealth = 0;
    }

    public FirstOfDay(JSONObject obj) throws JSONException
    {
        super(obj);
        sleepQuality = obj.getInt("sleepQuality");
        mentalHealth = obj.getInt("mentalHealth");
        physicalHealth = obj.getInt("physicalHealth");
    }

    @Override
    public JSONObject toJSON() throws JSONException
    {
            JSONObject obj = super.toJSON();
            obj.put("type", TYPE_ID);
            obj.put("sleepQuality", sleepQuality);
            obj.put("mentalHealth", mentalHealth);
            obj.put("physicalHealth", physicalHealth);
            return obj;
    }
}
