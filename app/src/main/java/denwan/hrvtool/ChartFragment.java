package denwan.hrvtool;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;

import java.sql.Array;
import java.util.ArrayList;
import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ChartFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ChartFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChartFragment extends Fragment {

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

    private LineChart m_lineChart;
    private LineDataSet m_dataSet;
    private ArrayList<Entry> m_chartEntries;
    //private OnFragmentInteractionListener mListener;

    public ChartFragment() {
        // Required empty public constructor
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

    @Override
    public void onStart() {
        super.onStart();

        DateTime dt[] = new DateTime[7];
        m_chartEntries = new ArrayList<>();
        for(int i = 0; i < 7; ++i)
        {
            DateTime today = DateTime.startOfToday(i - 6);
            float rmssd = Native.getAverageRmssd1(today.year, today.month, today.day) * 1000.f;

            m_chartEntries.add(new Entry(i, rmssd));

            dt[i] = today;
        }

        m_dataSet = new LineDataSet(m_chartEntries, "RMSSD");
        LineData lineData = new LineData(m_dataSet);
        m_lineChart.setData(lineData);
        m_lineChart.getDescription().setEnabled(false);
        XAxis xAxis = m_lineChart.getXAxis();
        xAxis.setValueFormatter(new MyYAxisValueFormatter(dt));
        xAxis.setTextSize(8.f);
        m_lineChart.invalidate(); // refresh
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_chart, container, false);

        m_lineChart = (LineChart)view.findViewById(R.id.chart);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction();
    }
}
