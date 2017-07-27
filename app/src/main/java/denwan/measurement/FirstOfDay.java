package denwan.measurement;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by tanne on 12.07.2017.
 */

public class FirstOfDay extends HRV implements Serializable {
    static final int TYPE_ID = 1;

    public int sleepQuality;
    public int mentalHealth;
    public int physicalHealth;

    public FirstOfDay()
    {
        super();
        sleepQuality = 0;
        mentalHealth = 0;
        physicalHealth = 0;
    }

    public FirstOfDay(HRV hrv)
    {
        super(hrv);
        sleepQuality = 0;
        mentalHealth = 0;
        physicalHealth = 0;
    }

    FirstOfDay(JSONObject obj) throws JSONException
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

    @Override
    public void read(ObjectInputStream in) throws IOException
    {
        super.read(in);

        sleepQuality = in.readInt();
        mentalHealth = in.readInt();
        physicalHealth = in.readInt();
    }

    @Override
    public void write(ObjectOutputStream out) throws IOException
    {
        out.writeInt(TYPE_ID);
        super.write(out);

        out.writeInt(sleepQuality);
        out.writeInt(mentalHealth);
        out.writeInt(physicalHealth);
    }
}
