package denwan.hrv;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

/**
 * Created by tanne on 19.06.2017.
 */

public class DateTime implements Parcelable {
    public int year;
    public int month;
    public int day;
    public int hour ;
    public int minute;

    public static DateTime now()
    {
        return new DateTime(Calendar.getInstance());
    }

    public static DateTime now(int offsetDays)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, offsetDays);
        return new DateTime(calendar);
    }

    public static DateTime startOfToday()
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return new DateTime(calendar);
    }

    public static DateTime startOfToday(int offsetDays)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        calendar.add(Calendar.DAY_OF_MONTH, offsetDays);

        return new DateTime(calendar);
    }

    public DateTime()
    {
        year = 0;
        month = 0;
        day = 0;
        hour = 0;
        minute = 0;
    }

    public DateTime(Calendar calendar)
    {
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR_OF_DAY);
        minute = calendar.get(Calendar.MINUTE);
    }

    public DateTime(int y, int m, int d, int hour, int minute)
    {
        year = y;
        month = m;
        day = d;
        this.hour = hour;
        this.minute = minute;
    }

    public DateTime(JSONObject json)
    {
        try {
            year = json.getInt("year");
            month = json.getInt("month");
            day = json.getInt("day");
            this.hour = json.getInt("hour");
            this.minute = json.getInt("minute");
        }
        catch(JSONException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public void createFromParcel(Parcel in)
    {
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        hour = in.readInt();
        minute = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeInt(hour);
        dest.writeInt(minute);
    }

    public JSONObject toJSON()
    {
        try {
            JSONObject obj = new JSONObject();
            obj.put("year", year);
            obj.put("month", month);
            obj.put("day", day);
            obj.put("hour", hour);
            obj.put("minute", minute);
            return obj;
        }
        catch(JSONException e)
        {
            throw new RuntimeException(e.toString());
        }
    }

    public static final Parcelable.Creator<DateTime> CREATOR =
            new Parcelable.Creator<DateTime>() {

                public DateTime createFromParcel(Parcel in)
                {
                    DateTime data = new DateTime();
                    data.createFromParcel(in);
                    return data;
                }

                public DateTime[] newArray(int size) {
                    return new DateTime[size];
                }
            };

    static public boolean lessThan(DateTime o1, DateTime o2)
    {
        return (o1.year < o2.year) ||
                (o1.year == o2.year && o1.month < o2.month) ||
                (o1.year == o2.year && o1.month == o2.month && o1.day < o2.day) ||
                (o1.year == o2.year && o1.month == o2.month && o1.day == o2.day && o1.hour < o2.hour) ||
                (o1.year == o2.year && o1.month == o2.month && o1.day == o2.day && o1.hour == o2.hour && o1.minute < o2.minute);
    }

    static public boolean equals(DateTime o1, DateTime o2)
    {
       return (o1.year == o2.year) &&
               (o1.month == o2.month) &&
               (o1.day == o2.day) &&
               (o1.hour == o2.hour) &&
               (o1.minute == o2.minute);
    }
}
