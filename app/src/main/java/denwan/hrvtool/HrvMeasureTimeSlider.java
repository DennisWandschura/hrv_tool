package denwan.hrvtool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tanne on 14.07.2017.
 */

public class HrvMeasureTimeSlider extends LinearLayout {

    LinearLayout m_layout = null;
    TextView m_textViewValue = null;
    SeekBar m_sb = null;

    public HrvMeasureTimeSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HrvMeasureTimeSlider);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        m_layout = (LinearLayout) li.inflate(R.layout.hrvmeasuretimeslider, this, true);

        m_textViewValue = (TextView)m_layout.findViewById(R.id.hrv_measure_slider_tv_value);

        m_sb = (SeekBar)m_layout.findViewById(R.id.hrv_measure_slider_seekBar);
        m_sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                Settings.DATA.measurementMinutes = 2.0f + (progress * 0.5f);
                m_textViewValue.setText(String.format("%.2f minutes", Settings.DATA.measurementMinutes));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        a.recycle();

        int progress = (int)((Settings.DATA.measurementMinutes - 2.f) * 2.f);
        m_sb.setProgress(progress);
        m_textViewValue.setText(String.format("%.2f minutes", Settings.DATA.measurementMinutes));
    }
}
