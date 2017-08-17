package denwan.hrvtool;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;


public class ChartFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    private enum OffsetType{Days, Weeks, Months};

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        SpinnerData data = (SpinnerData)parent.getItemAtPosition(position);
        updateChart(data);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private class MyYAxisValueFormatter implements IAxisValueFormatter {

        DateTime m_dateTimes[];

        MyYAxisValueFormatter(DateTime dt[]) {
            super();

            m_dateTimes = dt;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            // "value" represents the position of the label on the axis (x or y)

            int idx = (int)value;

            return String.format(Locale.getDefault(), "%02d.%02d.%d", m_dateTimes[idx].day, m_dateTimes[idx].month, m_dateTimes[idx].year);
        }
    }

    private class SpinnerData
    {
        OffsetType type;
        int offset;

        SpinnerData(OffsetType type, int offset)
        {
            this.type = type;
            this.offset=offset;
        }

        @Override
        public String toString()
        {
            String text = "Error";
            switch (type) {
                case Days:
                {
                    if (offset == 1)
                        text = "Day";
                    else
                        text = "Days";
                }break;
                case Weeks:
                {
                    if(offset == 1)
                        text = "Week";
                    else
                        text = "Weeks";
                }break;
                case Months:
                {
                    if(offset == 1)
                        text = "Month";
                    else
                        text = "Months";
                }break;
            }

            return String.format(Locale.getDefault(), "%d %s", offset, text);
        }
    }

    private LineChart m_lineChart;
    Spinner m_spinner;

    public ChartFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ChartFragment.
     */
    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void updateChartDataRMSSDOffsetDays(int offsetInDays)
    {
        Calendar today = Calendar.getInstance();
        int dayOfYear = today.get(Calendar.DAY_OF_YEAR);
        int startDayOfYear = dayOfYear - offsetInDays;

        Calendar current = Calendar.getInstance();
        current.set(Calendar.DAY_OF_YEAR, startDayOfYear);

        ArrayList<Entry> chartEntries = new ArrayList<>();
        DateTime dateTimes[] = new DateTime[offsetInDays];
        for(int i = 0; i < offsetInDays; ++i)
        {
            current.add(Calendar.DAY_OF_YEAR, 1);

            int year = current.get(Calendar.YEAR);
            int month = current.get(Calendar.MONTH);
            int day = current.get(Calendar.DAY_OF_MONTH);

            float rmssd = Native.getAverageRmssd1(year, month, day) * 1000.f;

            chartEntries.add(new Entry(i, rmssd));

            dateTimes[i] = new DateTime(year, month, day, 0, 0);
        }

        ////////////////////////////////////////

        XAxis xAxis = m_lineChart.getXAxis();
        xAxis.setValueFormatter(new MyYAxisValueFormatter(dateTimes));

        LineDataSet dataSet = new LineDataSet(chartEntries, "RMSSD");
        LineData lineData = new LineData(dataSet);
        lineData.setValueTextColor(0xFFFFFFFF);
        m_lineChart.setData(lineData);
    }

    void updateChart(SpinnerData data)
    {
        int offset = data.offset;
        switch (data.type) {
            case Days:
            {
                updateChartDataRMSSDOffsetDays(offset);
            }break;
            case Weeks:
            {
                int offsetInDays = offset * 7;
                updateChartDataRMSSDOffsetDays(offsetInDays);
            }break;
            case Months:
            {

            }break;
        }

        m_lineChart.getDescription().setEnabled(false);
        XAxis xAxis = m_lineChart.getXAxis();
        xAxis.setTextSize(8.f);
        xAxis.setTextColor(0xFFFFFFFF);

        m_lineChart.invalidate();
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        m_lineChart = (LineChart)view.findViewById(R.id.chart);
        m_spinner = (Spinner)view.findViewById(R.id.spinner);

        ArrayAdapter<SpinnerData> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item);
        adapter.add(new SpinnerData(OffsetType.Weeks, 1));
        adapter.add(new SpinnerData(OffsetType.Weeks, 2));
        adapter.add(new SpinnerData(OffsetType.Months, 1));
        adapter.add(new SpinnerData(OffsetType.Months, 3));
        adapter.add(new SpinnerData(OffsetType.Months, 6));
        m_spinner.setAdapter(adapter);
        m_spinner.setOnItemSelectedListener(this);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}
