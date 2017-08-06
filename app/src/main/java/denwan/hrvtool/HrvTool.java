package denwan.hrvtool;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;

import denwan.hrv.Native;

public class HrvTool extends AppCompatActivity implements HrvFragment.OnListFragmentInteractionListener, OverviewFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    //int m_selectedPage;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    OverviewFragment m_overviewFragment = null;
    HrvFragment m_hrvFragment = null;

    File getFile()
    {
        return new File( this.getExternalFilesDir(null), "hrv.bin");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hrv_tool);

        denwan.hrv.Native.initialize();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        File file = getFile();
        denwan.hrv.Native.loadData(file.getAbsolutePath());
        Settings.load(this);
        //denwan.measurement.Data.load(this);

       // m_selectedPage = 0;
    }

    protected void onStart()
    {
        super.onStart();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        Settings.save(this);

        File file = getFile();
        denwan.hrv.Native.saveData(file.getAbsolutePath());
        denwan.hrv.Native.shutdown();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hrv_tool, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(HrvEntryView item) {

        Intent intent = new Intent(this, ShowHrvActivity.class);
        intent.putExtra(Native.MEASUREMENT_IDX, item.m_idx);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onAttach(OverviewFragment fragment)
    {
        m_overviewFragment = fragment;
        if(m_hrvFragment != null && m_overviewFragment != null)
        {
            m_hrvFragment.setOverview(m_overviewFragment);
        }
    }

    public void onAttach(HrvFragment fragment)
    {
        m_hrvFragment = fragment;
        if(m_hrvFragment != null && m_overviewFragment != null)
        {
            m_hrvFragment.setOverview(m_overviewFragment);
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            if(position == 0)
            {
                return OverviewFragment.newInstance();
            }
            else if(position == 1)
            {
                return HrvFragment.newInstance();
            }
            else if(position == 2)
                return ChartFragment.newInstance();
            else if(position == 3)
                return SettingsFragment.newInstance();
            else
                return null;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: {
                    return "Overview";
                }
                case 1:
                    return "HRV";
                case 2:
                    return "Charts";
                case 3:
                    return "Settings";
            }
            return null;
        }
    }
}
