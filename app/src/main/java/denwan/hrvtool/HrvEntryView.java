package denwan.hrvtool;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;

/**
 * Created by tanne on 19.06.2017.
 */

public class HrvEntryView extends LinearLayout {

    TextView m_textDate;
    TextView m_textRmssd;
    TextView m_textLnRmssd;
    int m_idx;
    DateTime m_dateTime;

    public HrvEntryView(Context context)
    {
        super(context);
    }

    public HrvEntryView(Context context, int idx) {
        super(context);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.hrventryview, this, true);

        m_textDate = (TextView) layout.findViewById(R.id.hrv_entry_textViewDate);
        m_textRmssd = (TextView) layout.findViewById(R.id.hrv_entry_textViewRMSSD);
        m_textLnRmssd = (TextView) layout.findViewById(R.id.hrv_entry_textViewLnRMSSD);

        m_idx = idx;
        m_dateTime = Native.getDateTime(idx);

        setEntryData();
    }

    void setEntryData()
    {
        m_textDate.setText(String.format(Locale.getDefault(), "%02d.%02d.%d %02d:%02d", m_dateTime.day, m_dateTime.month, m_dateTime.year, m_dateTime.hour, m_dateTime.minute));
        float rmssd = Native.getRMSSD(m_idx) * 1000.0f;
        m_textRmssd.setText(String.format(Locale.getDefault(), "%.2f ms", rmssd));
        m_textLnRmssd.setText(String.format(Locale.getDefault(), "%.2f", Math.log(rmssd)));
    }
}
