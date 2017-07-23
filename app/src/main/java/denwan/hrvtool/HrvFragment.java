package denwan.hrvtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import denwan.hrv.DateTime;
import denwan.measurement.Data;
import denwan.measurement.FirstOfDay;
import denwan.measurement.HRV;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HrvFragment extends Fragment {

    LinearLayout m_hrvEntries = null;
    OverviewFragment m_fragmentOverview = null;
    private OnListFragmentInteractionListener mListener;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public HrvFragment() {
    }

    public void setOverview(OverviewFragment overview)
    {
        m_fragmentOverview=overview;
    }

    public static HrvFragment newInstance() {
        HrvFragment fragment = new HrvFragment();

        return fragment;
    }

    void onClickEntry(View v)
    {
        HrvEntryView view = (HrvEntryView)v;
        mListener.onFragmentInteraction(view);
    }

    void addEntryToList(HRV data, DateTime dateTime)
    {
        HrvEntryView entryView = new HrvEntryView(getContext(), data, dateTime);
        entryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEntry(v);
            }
        });
        m_hrvEntries.addView(entryView, 0);
    }

    void addHrvEntriesToList()
    {
        denwan.measurement.Data.foreachEntry(new Data.ForEachEntry()
        {
            @Override
            public void onEntry(DateTime dt, HRV hrv) {
                addEntryToList(hrv, dt);
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hrv, container, false);

        m_hrvEntries = (LinearLayout)view.findViewById(R.id.fragment_hrv_hrv_scroll_list);

        Button buttonMeasureHrv = (Button)view.findViewById(R.id.fragment_hrv_button_measure);
        buttonMeasureHrv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickMeasureHRV();
            }
        });

        addHrvEntriesToList();

        return view;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnListFragmentInteractionListener) {
            mListener = (OnListFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }

        mListener.onAttach(this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        m_fragmentOverview = null;
        mListener.onAttach(null);
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == denwan.measurement.HRV.MEASUREMENT_TAG_ID && resultCode == Activity.RESULT_OK)
        {
            DateTime key = data.getParcelableExtra(denwan.measurement.HRV.MEASUREMENT_TAG);
            denwan.measurement.HRV entry = denwan.measurement.Data.getEntry(key);

            if(entry.getClass() == FirstOfDay.class)
            {
                m_fragmentOverview.update_rmssd_today(entry);
            }

            m_fragmentOverview.update_rmssd_7day_avg();

            addEntryToList(entry, key);
        }
    }

    void onClickMeasureHRV()
    {
        Intent intent = new Intent(getContext(), MeasureHrvActivity.class);
        startActivityForResult(intent, denwan.measurement.HRV.MEASUREMENT_TAG_ID);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnListFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(HrvEntryView item);

        void onAttach(HrvFragment fragment);
    }
}
