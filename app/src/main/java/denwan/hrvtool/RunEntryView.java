package denwan.hrvtool;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import denwan.hrv.DateTime;
import denwan.hrv.RunEntryData;

/**
 * Created by tanne on 05.07.2017.
 */

public class RunEntryView extends LinearLayout
{
    TextView m_textDate;
    TextView m_textDistance;
    RunEntryData m_data;
    DateTime m_dateTime;

     public RunEntryView(Context context, RunEntryData data, DateTime dateTime) {
        super(context);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.runentryview, this, true);

         m_dateTime = dateTime;
         m_data = data;

         m_textDate = (TextView) layout.findViewById(R.id.run_entry_textViewDate);
         m_textDistance = (TextView) layout.findViewById(R.id.run_entry_textViewDistance);

         m_textDate.setText(String.format("%02d.%02d.%d %02d:%02d", m_dateTime.day, m_dateTime.month, m_dateTime.year, m_dateTime.hour, m_dateTime.minute));
         m_textDistance.setText(String.format("%.2f m", m_data.distanceMeters));
    }
}
