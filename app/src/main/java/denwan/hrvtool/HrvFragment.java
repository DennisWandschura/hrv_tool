package denwan.hrvtool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import java.util.ArrayList;

import denwan.hrv.Native;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnListFragmentInteractionListener}
 * interface.
 */
public class HrvFragment extends Fragment {

    static final short MEASUREMENT_TAG_ID = 111;

    LinearLayout m_hrvEntries = null;
    OverviewFragment m_fragmentOverview = null;
    ArrayList<HrvEntryView> m_hrv_views;
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
        return new HrvFragment();
    }

    void onClickEntry(View v)
    {
        HrvEntryView view = (HrvEntryView)v;
        mListener.onFragmentInteraction(view);
    }

    void addEntryToList(int idx, int offset)
    {
        HrvEntryView entryView = new HrvEntryView(getContext(), idx);
        entryView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickEntry(v);
            }
        });
        entryView.setHorizontalGravity(Gravity.CENTER_HORIZONTAL);
        m_hrvEntries.addView(entryView, offset);
        m_hrv_views.add(entryView);
    }

    void addHrvEntriesToList()
    {
        /*denwan.measurement.Data.foreachEntry(new Data.ForEachEntry()
        {
            @Override
            public void onEntry(DateTime dt, HRV hrv) {
                addEntryToList(hrv, dt);
            }
        });*/

        int entryCount = Native.getEntryCount();
        for(int i = 0; i < entryCount; ++i)
        {
            addEntryToList(i, 0);
        }
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
        m_hrv_views = new ArrayList<HrvEntryView>();

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
        if(requestCode == MEASUREMENT_TAG_ID && resultCode == Activity.RESULT_OK)
        {
            int idx = data.getIntExtra(Native.MEASUREMENT_IDX, -1);
            boolean updateIndices = data.getBooleanExtra(Native.UPDATE_INDICES, false);

            if(updateIndices)
            {
                for (HrvEntryView it : m_hrv_views) {
                    it.updateIndex();
                }
            }

            int isFirstOfDay = Native.isFirstOfDay(idx);
            if(isFirstOfDay == 1)
            {
                m_fragmentOverview.update_rmssd_today(idx);
            }

            m_fragmentOverview.update_rmssd_7day_avg();

            addEntryToList(idx, 0);
        }
    }

    void onClickMeasureHRV()
    {
        Intent intent = new Intent(getContext(), MeasureHrvActivity.class);
        startActivityForResult(intent, MEASUREMENT_TAG_ID);
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
