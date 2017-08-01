package denwan.hrv;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by tanne on 16.06.2017.
 */

public class RrReceiver implements AntPlusHeartRatePcc.ICalculatedRrIntervalReceiver
{
    public class Entry
    {
        long estTimestamp;
        public java.math.BigDecimal value;
        public AntPlusHeartRatePcc.RrFlag flag;

        Entry(long timestamp, BigDecimal v, AntPlusHeartRatePcc.RrFlag f){ estTimestamp = timestamp; value=v; flag=f;}


    };
    ArrayList<Entry> m_RRentries;

    public RrReceiver()
    {
        m_RRentries = new ArrayList<Entry>();
    }

    public void onNewCalculatedRrInterval(long estTimestamp,
                                          java.util.EnumSet<EventFlag> eventFlags,
                                          java.math.BigDecimal calculatedRrInterval, //ms
                                          AntPlusHeartRatePcc.RrFlag rrFlag)
    {
        switch (rrFlag) {
            case DATA_SOURCE_PAGE_4:
                break;
            case DATA_SOURCE_CACHED:
                break;
            case DATA_SOURCE_AVERAGED:
                break;
            case HEART_RATE_ZERO_DETECTED:
                return;
            case UNRECOGNIZED:
                return;
        }
        m_RRentries.add(new Entry(estTimestamp, calculatedRrInterval, rrFlag));
    }

    double getStandardDeviation(double values[])
    {
        double avg = 0.0;

        int count = values.length;
        for(int i = 0; i < count; ++i)
        {
            avg += values[i];
        }

        avg /= (double)count;

        double variance = 0.0;
        for(int i = 0; i  < count; ++i)
        {
            double tmp = values[i] - avg;
            variance += (tmp * tmp);
        }

        variance /= (double)count;

        return Math.sqrt(variance);
    }

    public float[] getHrvData()
    {
        int n = m_RRentries.size();
        float rr_values[] = new float[n];

        int i = 0;
        for(Entry iter : m_RRentries)
        {
            float value = (float)iter.value.doubleValue();
            // convert to seconds
            rr_values[i++] = value / 1000.0f;
        }

       return rr_values;
    }
}
