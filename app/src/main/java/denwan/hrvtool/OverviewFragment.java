package denwan.hrvtool;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OverviewFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OverviewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OverviewFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match

    // TODO: Rename and change types of parameters
    TextView m_tv_rmssd_today = null;
    TextView m_tv_rmssd_7davg = null;
    LinearLayout m_layout_hr_zones;
    private OnFragmentInteractionListener mListener;

    public OverviewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment OverviewFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OverviewFragment newInstance() {
        OverviewFragment fragment = new OverviewFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void update_rmssd_7day_avg()
    {
        DateTime start = DateTime.startOfToday(-7);
        DateTime end = DateTime.startOfToday();

        m_tv_rmssd_7davg.setText(String.format(Locale.getDefault(), "%.2f", Native.getAverageRmssd(start.year, start.month, start.day, end.year, end.month, end.day) * 1000.f));
    }

    void update_rmssd_today()
    {
        DateTime dateTime = new DateTime(Calendar.getInstance());

        int firstOfTodayIdx = Native.getFirstOfToday(dateTime.year, dateTime.month, dateTime.day);
        update_rmssd_today(firstOfTodayIdx);
    }

    public void update_rmssd_today(int firstOfTodayIdx)
    {
        if(firstOfTodayIdx >= 0)
        {
            m_tv_rmssd_today.setText(String.format(Locale.getDefault(), "%.2f", Native.getRMSSD(firstOfTodayIdx) * 1000.f));
        }
        else
            m_tv_rmssd_today.setText("0");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_overview, container, false);
        m_tv_rmssd_today = (TextView)view.findViewById(R.id.overview_tv_rmssd_today);
        m_tv_rmssd_7davg = (TextView)view.findViewById(R.id.overview_tv_rmssd_7davg);
        m_layout_hr_zones = (LinearLayout)view.findViewById(R.id.overview_time_in_zones);

        /*HeartRateZones zones = Activity.DATA.getZones();
        zones.setDefaults();
        zones.setThreshold(168);
        for(int i = 0; i < zones.getCount(); ++i)
        {
            m_layout_hr_zones.addView(new ZoneView(getContext(), zones.getZone(i)));
        }*/

        update_rmssd_today();

        update_rmssd_7day_avg();

        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }

        mListener.onAttach(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onAttach(null);
        mListener = null;
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
        void onFragmentInteraction(Uri uri);

        void onAttach(OverviewFragment fragment);
    }
}
