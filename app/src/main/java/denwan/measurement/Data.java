package denwan.measurement;

import android.app.Activity;
import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import denwan.hrv.DateTime;

/**
 * Created by tanne on 12.07.2017.
 */

public class Data {
    static class Date
    {
        int year, month, day;

        Date(DateTime dt)
        {
            year = dt.year;
            month = dt.month;
            day = dt.day;
        }

        Date(Calendar calendar)
        {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        Date(JSONObject obj) throws JSONException
        {
            year = obj.getInt("year");
            month = obj.getInt("month");
            day = obj.getInt("day");
        }

        private JSONObject toJSON() throws JSONException
        {
                JSONObject obj = new JSONObject();
                obj.put("year", year);
                obj.put("month", month);
                obj.put("day", day);
                return obj;
        }

        static private boolean lessThan(Date o1, Date o2)
        {
            return (o1.year < o2.year) ||
                    (o1.year == o2.year && o1.month < o2.month) ||
                    (o1.year == o2.year && o1.month == o2.month && o1.day < o2.day);
        }

        static private boolean equals(Date o1, Date o2)
        {
            return (o1.year == o2.year) &&
                    (o1.month == o2.month) &&
                    (o1.day == o2.day);
        }
    }

    static private class DateComp implements Comparator<Date>
    {

        @Override
        public int compare(Date o1, Date o2) {
            if(Date.equals(o1, o2))
                return 0;

            // sort newest dates to later
            return (Date.lessThan(o1, o2)) ? -1 : 1;
        }

        @Override
        public boolean equals(Object o)
        {
            return (o.getClass() == DateComp.class);
        }
    }

    static private class DateTimeComp implements Comparator<DateTime>
    {
        @Override
        public int compare(DateTime o1, DateTime o2) {
            if(DateTime.equals(o1, o2))
                return 0;

            return (DateTime.lessThan(o1, o2)) ? -1 : 1;
        }

        @Override
        public boolean equals(Object o)
        {
            return (o.getClass() == DateTimeComp.class);
        }
    }

    static private String FILENAME = "hrv.json";
    static private TreeMap<DateTime, HRV> ENTRIES = new TreeMap<>(new DateTimeComp());
    static private TreeMap<Date, DateTime> ENTRIES_PER_DAY = new TreeMap<>(new DateComp());

    static private boolean addEntryFirstOfDay(DateTime key)
    {
        Date date = new Date(key);
        DateTime firstOfDay = ENTRIES_PER_DAY.get(date);
        boolean isFirstOfDay = false;
        if(firstOfDay != null)
        {
            if(DateTime.lessThan(key, firstOfDay))
            {
                ENTRIES_PER_DAY.put(date, key);
                isFirstOfDay = true;
            }
        }
        else
        {
            ENTRIES_PER_DAY.put(date, key);
            isFirstOfDay = true;
        }

        return isFirstOfDay;
    }

    public static boolean addEntry(DateTime key, HRV data)
    {
        boolean isFirstOfDay = addEntryFirstOfDay(key);

        ENTRIES.put(key, data);

        return isFirstOfDay;
    }

    public static HRV getEntry(DateTime key)
    {
        return ENTRIES.get(key);
    }

    public static HRV getFirstOfToday()
    {
        Date date = new Date(Calendar.getInstance());

        DateTime firstOfDay = ENTRIES_PER_DAY.get(date);
        if(firstOfDay != null)
        {
            return getEntry(firstOfDay);
        }
        else
            return null;
    }

    public static boolean isFirstOfDay(DateTime key)
    {
        Date date = new Date(key);
        DateTime firstOfDay = ENTRIES_PER_DAY.get(date);

        return (firstOfDay == null) || DateTime.lessThan(key, firstOfDay);
    }

    static private File getPath(Activity activity)
    {
        return activity.getExternalFilesDir(null);
    }

    static private JSONArray entriesToJSON() throws JSONException
    {
            JSONArray obj = new JSONArray();

            for (Map.Entry<DateTime, HRV> it : ENTRIES.entrySet())
            {
                JSONObject set = new JSONObject();
                set.put("dateTime", it.getKey().toJSON());
                set.put("hrv", it.getValue().toJSON());
                obj.put(set);
            }

            return obj;
    }

    static private HRV create_HRV_from_JSON(JSONObject obj) throws JSONException
    {
        int type = obj.getInt("type");
        switch (type) {
            case HRV.TYPE_ID:
                return new HRV(obj);
            case FirstOfDay.TYPE_ID:
                return new FirstOfDay(obj);
            default:
                return null;
        }
    }

    static private void loadEntriesFromJSON(JSONObject root, String key) throws JSONException
    {
            JSONArray obj = root.getJSONArray(key);
            int size = obj.length();
            for (int i = 0; i < size; ++i) {
                JSONObject set = obj.getJSONObject(i);
                DateTime dateTime = new DateTime(set.getJSONObject("dateTime"));
                ENTRIES.put(dateTime, create_HRV_from_JSON(set.getJSONObject("hrv")));
            }
    }

    static private void loadEntriesPerDayFromJSON(JSONObject root, String key) throws JSONException
    {
        JSONArray obj = root.getJSONArray(key);
        int size = obj.length();
        for (int i = 0; i < size; ++i) {
            JSONObject set = obj.getJSONObject(i);
            ENTRIES_PER_DAY.put(new Date(set.getJSONObject("date")), new DateTime(set.getJSONObject("dateTime")));
        }
    }

    static private JSONArray entriesPerDayToJSON()
    {
        try {
            JSONArray obj = new JSONArray();

            for (Map.Entry<Date, DateTime> it : ENTRIES_PER_DAY.entrySet())
            {
                JSONObject set = new JSONObject();
                set.put("date", it.getKey().toJSON());
                set.put("dateTime", it.getValue().toJSON());
                obj.put(set);
            }

            return obj;
        }
        catch(JSONException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public static boolean save(Activity activity)
    {
        File path = getPath(activity);
        path.mkdir();

        File file = new File(path, FILENAME);

        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("entries", entriesToJSON());
            jsonObject.put("entriesPerDay", entriesPerDayToJSON());

            FileWriter outfile = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(outfile);
            out.write(jsonObject.toString());
            out.close();
            outfile.close();
        }
        catch (JSONException e)
        {
            return false;
        }
        catch (java.io.IOException e)
        {
            return false;
        }

        return true;
    }

    static private String load_JSON_file(Activity activity) throws IOException
    {
        File file = new File(getPath(activity), FILENAME);
        FileInputStream infile = new FileInputStream(file);
        int size = infile.available();
        byte[] buffer = new byte[size];
        int readSize = infile.read(buffer);
        infile.close();
        String json = new String(buffer, "UTF-8");
        return json;
    }

    public static boolean load(Activity activity)
    {
        try {
            String json_data = load_JSON_file(activity);
            JSONObject obj = new JSONObject(json_data);

            loadEntriesFromJSON(obj, "entries");
            loadEntriesPerDayFromJSON(obj, "entriesPerDay");
        }
        catch(JSONException e)
        {
            return false;
        }
        catch (IOException ex)
        {
            return false;
        }

        return true;
    }

    public interface ForEachEntry
    {
        void onEntry(DateTime dt, HRV hrv);
    }

    static public void foreachEntry(ForEachEntry f)
    {
        for (Map.Entry<DateTime, HRV> it : ENTRIES.entrySet())
        {
            f.onEntry(it.getKey(), it.getValue());
        }
    }

    static public SortedMap<DateTime, HRV> subMap(DateTime from, DateTime to)
    {
        return ENTRIES.subMap(from, to);
    }
}
