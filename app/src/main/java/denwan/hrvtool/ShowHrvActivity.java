package denwan.hrvtool;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import denwan.hrv.DateTime;
import denwan.measurement.FirstOfDay;

public class ShowHrvActivity extends AppCompatActivity {

    denwan.measurement.HRV m_entryData = null;
    FirstOfDay m_firstOfDay = null;
    boolean m_isFirstOfDay = false;
    int m_resultCode = 0;

    void setTextViewText(int id, String text)
    {
        TextView tv = (TextView)findViewById(id);
        tv.setText(text);
    }

    void setTextViewText(int id, String text,double v)
    {
        setTextViewText(id, text + String.format("%.2f", v));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_hrv);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DateTime key = null;
        String error = new String("None");
        try {
            key = getIntent().getParcelableExtra(denwan.measurement.HRV.MEASUREMENT_TAG);
        }
        catch (Exception e)
        {
            error = e.toString();
        }

        m_entryData = denwan.measurement.Data.getEntry(key);
        m_firstOfDay = null;

        setTextViewText(R.id.textViewSDNN, "SDNN: ", m_entryData.sdnn);
        setTextViewText(R.id.textViewRMSSD, "RMSSD: ",m_entryData.rmssd);
        setTextViewText(R.id.textViewSDSD, "SDSD: ", m_entryData.sdsd);
        setTextViewText(R.id.textViewPNN50,"PNN50: ", m_entryData.pnn50);
        setTextViewText(R.id.textViewPNN20, "PNN20: ",m_entryData.pnn20);

        Button buttonClose = (Button)findViewById(R.id.buttonCloseHrvView);
        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                });
            }
        });

        CustomSlider sliderMental = (CustomSlider)findViewById(R.id.customSliderMental);
        CustomSlider sliderPhysical = (CustomSlider)findViewById(R.id.customSliderPhysical);
        CustomSlider sliderSleep = (CustomSlider)findViewById(R.id.customSliderSleep);
        if(m_entryData.getClass() == denwan.measurement.FirstOfDay.class)
        {
            m_firstOfDay = (FirstOfDay) m_entryData;

            sliderMental.setVisibility(View.VISIBLE);
            sliderPhysical.setVisibility(View.VISIBLE);
            sliderSleep.setVisibility(View.VISIBLE);

            sliderMental.setProgress(m_firstOfDay.mentalHealth);
            sliderMental.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    m_firstOfDay.mentalHealth = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });


            sliderPhysical.setProgress(m_firstOfDay.physicalHealth);
            sliderPhysical.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    m_firstOfDay.physicalHealth = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            sliderSleep.setProgress(m_firstOfDay.sleepQuality);
            sliderSleep.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    m_firstOfDay.sleepQuality = progress;
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
