package denwan.hrvtool;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dsi.ant.plugins.antplus.pcc.AntPlusHeartRatePcc;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceState;
import com.dsi.ant.plugins.antplus.pcc.defines.EventFlag;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;
import com.dsi.ant.plugins.antplus.pccbase.AntPluginPcc;
import com.dsi.ant.plugins.antplus.pccbase.PccReleaseHandle;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;
import denwan.hrv.RrReceiver;
import denwan.hrv.HeartRateDeviceSearch;

public class MeasureHrvActivity extends AppCompatActivity
{
    public static final int SHOW_HRV_MEASUREMENT_RESULT = 999;

    TextView m_tv_hr;
    CountDownTimer m_timer;
    ProgressBar m_progressBar;
    TextView m_tv_progress;
    AntPlusHeartRatePcc m_hrPcc = null;
    protected PccReleaseHandle<AntPlusHeartRatePcc> m_releaseHandle = null;
    RrReceiver m_rrReceiver;
    HeartRateDeviceSearch m_search;
    DateTime m_measurementTime = null;
    boolean m_measuring = false;
    int m_index;

    void releaseHandle()
    {
        if(m_releaseHandle != null)
            m_releaseHandle.close();

        m_releaseHandle = null;
    }

    public void setProgressBarProgress(int p)
    {
        m_progressBar.setProgress(p);
        m_tv_progress.setText(String.format(Locale.getDefault(), "Progress: %d", p));
    }

    private void startMeasurement()
    {
        if(!m_measuring)
        {
            m_index = -1;
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            m_measuring = true;

            releaseHandle();

            m_measurementTime = new DateTime(Calendar.getInstance());

            m_releaseHandle = AntPlusHeartRatePcc.requestAccess(this, Settings.DATA.antDeviceNumber, 0,
                    base_IPluginAccessResultReceiver, base_IDeviceStateChangeReceiver);
        }
    }

    void stopMeasurement()
    {
        m_measuring = false;

        m_hrPcc.releaseAccess();
        releaseHandle();

        float rr[] = m_rrReceiver.getHrvData();
        int firstOfTodayIdx = Native.getFirstOfToday(m_measurementTime.year, m_measurementTime.month, m_measurementTime.day);
        boolean isFirstOfDay = (firstOfTodayIdx == -1);

        m_index = denwan.hrv.Native.createNewEntry(m_measurementTime.year, m_measurementTime.month, m_measurementTime.day, m_measurementTime.hour, m_measurementTime.minute, rr, isFirstOfDay);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = new Intent(this, ShowHrvActivity.class);
        intent.putExtra(Native.MEASUREMENT_IDX, m_index);
        startActivityForResult(intent, SHOW_HRV_MEASUREMENT_RESULT);
    }

    public void onDeviceFound(HeartRateDeviceSearch.MultiDeviceSearchResultWithRSSI result)
    {
        TextView tv_device = (TextView)findViewById(R.id.textViewDevice);
        tv_device.setText(result.mDevice.getDeviceDisplayName());
        Settings.DATA.antDeviceNumber = result.mDevice.getAntDeviceNumber();

        startMeasurement();
    }

    public void onRequestAccessFailure()
    {
        Settings.DATA.antDeviceNumber = 0;
    }

    private void onButtonStart()
    {
        if( Settings.DATA.antDeviceNumber == 0) {
            m_search = new HeartRateDeviceSearch(this, new HeartRateDeviceSearch.OnDeviceFoundCallback() {
                public void run(final HeartRateDeviceSearch.MultiDeviceSearchResultWithRSSI result) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onDeviceFound(result);
                        }
                    });
                }
            });
        }
        else
        {
            startMeasurement();
        }
    }

    void subscribeToHrEvents()
    {
        m_hrPcc.subscribeHeartRateDataEvent(new AntPlusHeartRatePcc.IHeartRateDataReceiver() {
            @Override
            public void onNewHeartRateData(final long estTimestamp, EnumSet<EventFlag> eventFlags,
                                           final int computedHeartRate, final long heartBeatCount,
                                           final BigDecimal heartBeatEventTime, final AntPlusHeartRatePcc.DataState dataState) {

                final boolean isInitialValue = AntPlusHeartRatePcc.DataState.ZERO_DETECTED.equals(dataState);
                // Mark heart rate with asterisk if zero detected
                final String textHeartRate = String.valueOf(computedHeartRate)
                        + (isInitialValue ? "*" : "");


                // Mark heart beat count and heart beat event time with asterisk if initial value
                //final String textHeartBeatCount = String.valueOf(heartBeatCount)
                //        + (isInitialValue ? "*" : "");
                //final String textHeartBeatEventTime = String.valueOf(heartBeatEventTime)
                //        + (isInitialValue ? "*" : "");

                /*
                update ui
                 */

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        m_tv_hr.setText(textHeartRate);
                    }
                });
            }
        });

        //long estTimestamp, java.util.EnumSet<EventFlag> eventFlags, java.math.BigDecimal calculatedRrInterval, AntPlusHeartRatePcc.RrFlag rrFlag
        m_rrReceiver = new RrReceiver();
        m_hrPcc.subscribeCalculatedRrIntervalEvent(m_rrReceiver);

        m_timer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_measure_hrv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        m_progressBar = (ProgressBar)findViewById(R.id.progressBar2);
        m_tv_progress = (TextView) findViewById(R.id.textViewProgress);
        m_tv_hr = (TextView) findViewById(R.id.textViewHeartRate);

        final float measurementTimeMiliseconds = (60000.f * Settings.DATA.measurementMinutes);
        m_timer = new CountDownTimer((int)measurementTimeMiliseconds, 60) {
            @Override
            public void onTick(long millisUntilFinished)
            {
                final float progress = ((measurementTimeMiliseconds - (float)millisUntilFinished) / measurementTimeMiliseconds) * 100.0f;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarProgress((int)progress);
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setProgressBarProgress(100);
                        stopMeasurement();
                    }
                });
            }
        };

        Button buttonStart = (Button)findViewById(R.id.buttonStart);
        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View view)
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onButtonStart();
                    }
                });
            }
        });


        TextView tv_device = (TextView)findViewById(R.id.textViewDevice);
        tv_device.setText(String.format(Locale.getDefault(), "Device: %d", Settings.DATA.antDeviceNumber));

        m_index = -1;
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onStop() {
        super.onStop();

        if(m_measuring)
            stopMeasurement();
    }

    protected void onDestroy()
    {
        super.onDestroy();

        if(m_measuring)
            stopMeasurement();

        releaseHandle();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SHOW_HRV_MEASUREMENT_RESULT && resultCode == RESULT_OK)
        {
            Intent intent = new Intent();
            intent.putExtra(Native.MEASUREMENT_IDX, m_index);
            setResult(RESULT_OK, intent);
            this.finish();
        }
    }

    AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc> base_IPluginAccessResultReceiver =
            new AntPluginPcc.IPluginAccessResultReceiver<AntPlusHeartRatePcc>()
            {
                //Handle the result, connecting to events on success or reporting failure to user.
                @Override
                public void onResultReceived(AntPlusHeartRatePcc result, RequestAccessResult resultCode,
                                             DeviceState initialDeviceState)
                {
                    switch(resultCode)
                    {
                        case SUCCESS:
                            m_hrPcc = result;
                            //tv_status.setText(result.getDeviceName() + ": " + initialDeviceState);
                            //showDataDisplay(result.getDeviceName() + ": " + initialDeviceState);
                            subscribeToHrEvents();
                            // if(!result.supportsRssi())
                            //    tv_rssi.setText("N/A");
                            break;
                        case CHANNEL_NOT_AVAILABLE:
                            // Toast.makeText(Activity_HeartRateDisplayBase.this, "Channel Not Available", Toast.LENGTH_SHORT).show();
                            //  tv_status.setText("Error. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                        case ADAPTER_NOT_DETECTED:
                            //  Toast.makeText(Activity_HeartRateDisplayBase.this, "ANT Adapter Not Available. Built-in ANT hardware or external adapter required.", Toast.LENGTH_SHORT).show();
                            //  tv_status.setText("Error. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                        case BAD_PARAMS:
                            //Note: Since we compose all the params ourself, we should never see this result
                            //  Toast.makeText(Activity_HeartRateDisplayBase.this, "Bad request parameters.", Toast.LENGTH_SHORT).show();
                            //  tv_status.setText("Error. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                        case OTHER_FAILURE:
                            // Toast.makeText(Activity_HeartRateDisplayBase.this, "RequestAccess failed. See logcat for details.", Toast.LENGTH_SHORT).show();
                            //  tv_status.setText("Error. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                        case DEPENDENCY_NOT_INSTALLED:
                            onRequestAccessFailure();
                           /* tv_status.setText("Error. Do Menu->Reset.");
                            AlertDialog.Builder adlgBldr = new AlertDialog.Builder(Activity_HeartRateDisplayBase.this);
                            adlgBldr.setTitle("Missing Dependency");
                            adlgBldr.setMessage("The required service\n\"" + AntPlusHeartRatePcc.getMissingDependencyName() + "\"\n was not found. You need to install the ANT+ Plugins service or you may need to update your existing version if you already have it. Do you want to launch the Play Store to get it?");
                            adlgBldr.setCancelable(true);
                            adlgBldr.setPositiveButton("Go to Store", new OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    Intent startStore = null;
                                    startStore = new Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=" + AntPlusHeartRatePcc.getMissingDependencyPackageName()));
                                    startStore.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                    Activity_HeartRateDisplayBase.this.startActivity(startStore);
                                }
                            });
                            adlgBldr.setNegativeButton("Cancel", new OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }
                            });

                            final AlertDialog waitDialog = adlgBldr.create();
                            waitDialog.show();*/
                            break;
                        case USER_CANCELLED:
                            // tv_status.setText("Cancelled. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                        case UNRECOGNIZED:
                           /* Toast.makeText(Activity_HeartRateDisplayBase.this,
                                    "Failed: UNRECOGNIZED. PluginLib Upgrade Required?",
                                    Toast.LENGTH_SHORT).show();
                            tv_status.setText("Error. Do Menu->Reset.");*/
                            onRequestAccessFailure();
                            break;
                        default:
                            //  Toast.makeText(Activity_HeartRateDisplayBase.this, "Unrecognized result: " + resultCode, Toast.LENGTH_SHORT).show();
                            // tv_status.setText("Error. Do Menu->Reset.");
                            onRequestAccessFailure();
                            break;
                    }
                }
            };

    //Receives state changes and shows it on the status display line
    AntPluginPcc.IDeviceStateChangeReceiver base_IDeviceStateChangeReceiver =
            new AntPluginPcc.IDeviceStateChangeReceiver()
            {
                @Override
                public void onDeviceStateChange(final DeviceState newDeviceState)
                {
                }
            };
}
