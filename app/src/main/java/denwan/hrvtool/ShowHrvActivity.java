package denwan.hrvtool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

import denwan.hrv.DateTime;
import denwan.hrv.Native;

public class ShowHrvActivity extends AppCompatActivity {

    //denwan.measurement.HRV m_entryData = null;
   // FirstOfDay m_firstOfDay = null;
    int m_index;
    boolean m_isFirstOfDay = false;
    int m_resultCode = 0;

    void setTextViewText(int id, String text)
    {
        TextView tv = (TextView)findViewById(id);
        tv.setText(text);
    }

    void setTextViewText(int id, String text, float v)
    {
        setTextViewText(id, String.format(Locale.getDefault(), text, v));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hrv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        m_index = getIntent().getIntExtra(Native.MEASUREMENT_IDX, -1);

        if(m_index == -1)
        {
            setResult(RESULT_CANCELED, null);
            finish();
        }

        int isFirstOfDay = Native.isFirstOfDay(m_index);
        if(isFirstOfDay == -1)
        {
            setResult(RESULT_CANCELED, null);
            finish();
        }

        setTextViewText(R.id.textViewSDNN, "SDNN: %.2f ms", Native.getSDNN(m_index) * 1000.f);
        setTextViewText(R.id.textViewRMSSD, "RMSSD: %.3f ms", Native.getRMSSD(m_index) * 1000.f);
        setTextViewText(R.id.textViewPNN50,"PNN50: %.2f", Native.getPNN50(m_index) * 100.f);
        setTextViewText(R.id.textViewPNN20, "PNN20: %.2f", Native.getPNN20(m_index) * 100.f);

        DateTime dateTime = Native.getDateTime(m_index);
        setTextViewText(R.id.textViewHRVEntry, String.format(Locale.getDefault(), "%d.%d.%d %d:%d", dateTime.day, dateTime.month, dateTime.year, dateTime.hour, dateTime.minute));

        CustomSlider sliderMental = (CustomSlider)findViewById(R.id.customSliderMental);
        CustomSlider sliderPhysical = (CustomSlider)findViewById(R.id.customSliderPhysical);
        CustomSlider sliderSleep = (CustomSlider)findViewById(R.id.customSliderSleep);
        if(isFirstOfDay == 1)
        {
            sliderMental.setVisibility(View.VISIBLE);
            sliderPhysical.setVisibility(View.VISIBLE);
            sliderSleep.setVisibility(View.VISIBLE);

            sliderMental.setProgress(Native.getMental(m_index));
            sliderMental.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Native.setMental(m_index, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });


            sliderPhysical.setProgress(Native.getPhysical(m_index));
            sliderPhysical.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Native.setPhysical(m_index, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            sliderSleep.setProgress(Native.getSleep(m_index));
            sliderSleep.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    Native.setSleep(m_index, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
        }
        else
        {
            sliderMental.setVisibility(View.INVISIBLE);
            sliderPhysical.setVisibility(View.INVISIBLE);
            sliderSleep.setVisibility(View.INVISIBLE);
        }

        setResult(RESULT_OK, null);
    }

    protected void onStart()
    {
        super.onStart();
    }

    protected void onPause()
    {
        super.onPause();
    }

    protected void onStop()
    {
        super.onStop();
    }

    protected void onDestroy()
    {
        super.onDestroy();
    }
}
