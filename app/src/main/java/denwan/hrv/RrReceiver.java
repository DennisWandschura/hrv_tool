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

    public denwan.measurement.HRV getHrvData()
    {
        denwan.measurement.HRV result = new denwan.measurement.HRV();
        int n = m_RRentries.size();
        double rr_values[] = new double[n];

        int i = 0;
        for(Entry iter : m_RRentries)
        {
            double value = iter.value.doubleValue();
            rr_values[i++] = value;
        }

        double sd_values[] = new double[n - 1];
        double rmssd = 0.0;
        double diff;
        int nn50 = 0;
        int nn20 = 0;
        int sd_count = n - 1;
        for(i = 0; i  < sd_count; ++i)
        {
            diff = rr_values[i + 1] - rr_values[i];

            sd_values[i] = diff;

            rmssd += (diff * diff);

            diff = Math.abs(diff);

            if(diff > 50.0)
                ++nn50;

            if(diff > 20.0)
                ++nn20;
        }

        rmssd = Math.sqrt(rmssd / (double)sd_count); // in ms;

        result.sdnn = getStandardDeviation(rr_values);
        result.rmssd = rmssd;
        result.sdsd = getStandardDeviation(sd_values);
        result.nn50 = nn50;
        result.nn20 = nn20;
        result.pnn20 = (double)nn20 / (double)sd_count;
        result.pnn50 = (double)nn50 / (double)sd_count;
        //result.values = rr_values;

        return result;
    }
}
