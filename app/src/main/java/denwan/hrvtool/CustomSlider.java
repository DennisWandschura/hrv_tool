package denwan.hrvtool;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Created by tanne on 17.06.2017.
 */

public class CustomSlider extends RelativeLayout {
    RelativeLayout m_layout;
    TextView m_textViewHeader;
    TextView m_textViewMinText;
    TextView m_textViewMaxText;
    SeekBar m_seekbar;

    public CustomSlider(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CustomSlider);

        String minText = a.getString(R.styleable.CustomSlider_minText);
        String maxText = a.getString(R.styleable.CustomSlider_maxText);
        String headerText = a.getString(R.styleable.CustomSlider_headerText);
        int maxValue = a.getInteger(R.styleable.CustomSlider_maxValue, 10);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        m_layout = (RelativeLayout) li.inflate(R.layout.customslider, this, true);

        m_textViewMinText = (TextView) m_layout.findViewById(R.id.textViewMinText);
        m_textViewMaxText = (TextView) m_layout.findViewById(R.id.textViewMaxText);
        m_textViewHeader = (TextView) m_layout.findViewById(R.id.textViewHeader);
        m_seekbar = (SeekBar) m_layout.findViewById(R.id.customSliderSeekBar);

        m_textViewMinText.setText(minText);
        m_textViewMaxText.setText(maxText);
        m_textViewHeader.setText(headerText);

        m_seekbar.setMax(maxValue);

        a.recycle();
    }

    public void setProgress(int progress)
    {
        m_seekbar.setProgress(progress);
    }

    public int getProgress()
    {
        return m_seekbar.getProgress();
    }

    public void setOnSeekBarChangeListener (SeekBar.OnSeekBarChangeListener l)
    {
        m_seekbar.setOnSeekBarChangeListener(l);
    }
}
