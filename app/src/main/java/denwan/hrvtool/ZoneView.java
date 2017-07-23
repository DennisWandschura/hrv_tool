package denwan.hrvtool;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by tanne on 18.07.2017.
 */

public class ZoneView extends LinearLayout {
    TextView m_name;
    int m_backgroundColor;

    public ZoneView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.zone, this, true);

        m_name = (TextView)layout.findViewById(R.id.zone_name);
        m_backgroundColor = Color.WHITE;
    }

    public ZoneView(Context context, denwan.activity.HeartRateZones.Zone zone) {
        super(context);

        String service = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(service);
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.zone, this, true);

        m_name = (TextView)layout.findViewById(R.id.zone_name);
        m_name.setText(zone.name);

        m_backgroundColor = zone.color;

        setBackgroundColor(m_backgroundColor);
    }
}
