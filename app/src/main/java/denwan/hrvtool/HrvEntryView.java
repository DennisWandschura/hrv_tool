package denwan.hrvtool;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import denwan.hrv.DateTime;
import denwan.measurement.HRV;

/**
 * Created by tanne on 19.06.2017.
 */

public class HrvEntryView extends LinearLayout {

    TextView m_textDate;
    TextView m_textRmssd;
    HRV m_data;
    DateTime m_dateTime;

    public HrvEntryView(Context context, HRV data, DateTime dateTime) {
        super(context);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.hrventryview, this, true);

        m_textDate = (TextView) layout.findViewById(R.id.hrv_entry_textViewDate);
        m_textRmssd = (TextView) layout.findViewById(R.id.hrv_entry_textViewRMSSD);

        m_data = data;
        m_dateTime = dateTime;

        setEntryData();
    }

    void setEntryData()
    {
        m_textDate.setText(String.format("%02d.%02d.%d %02d:%02d", m_dateTime.day, m_dateTime.month, m_dateTime.year, m_dateTime.hour, m_dateTime.minute));
        m_textRmssd.setText(String.format("%.2f", m_data.rmssd));
    }
}
