package denwan.hrv;

import android.content.Context;

import java.util.EnumSet;
import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch;
import com.dsi.ant.plugins.antplus.pcc.defines.DeviceType;
import com.dsi.ant.plugins.antplus.pccbase.MultiDeviceSearch.MultiDeviceSearchResult;
import com.dsi.ant.plugins.antplus.pcc.MultiDeviceSearch.RssiSupport;
import com.dsi.ant.plugins.antplus.pcc.defines.RequestAccessResult;

/**
 * Created by tanne on 16.06.2017.
 */

public class HeartRateDeviceSearch {
    public class MultiDeviceSearchResultWithRSSI
    {
        public MultiDeviceSearchResult mDevice;
        public int mRSSI = Integer.MIN_VALUE;
    }

    public interface OnDeviceFoundCallback
    {
        void run(MultiDeviceSearchResultWithRSSI result);
    };

    MultiDeviceSearch m_search;
    OnDeviceFoundCallback m_callback;

    public HeartRateDeviceSearch(Context context, OnDeviceFoundCallback callback)
    {
        EnumSet<DeviceType> deviceTypes = EnumSet.of(DeviceType.HEARTRATE);
        m_callback=callback;
        m_search = new MultiDeviceSearch(context, deviceTypes, mCallback, mRssiCallback);
    }

    private MultiDeviceSearch.SearchCallbacks mCallback = new MultiDeviceSearch.SearchCallbacks() {
        public void onDeviceFound(final MultiDeviceSearchResult deviceFound)
        {
            final MultiDeviceSearchResultWithRSSI result = new MultiDeviceSearchResultWithRSSI();
            result.mDevice = deviceFound;

            m_callback.run(result);
        }

        public void onSearchStopped(RequestAccessResult reason)
        {
        }

        public void onSearchStarted(RssiSupport supportsRssi) {
            if(supportsRssi == RssiSupport.UNAVAILABLE)
            {
                // Toast.makeText(mContext, "Rssi information not available.", Toast.LENGTH_SHORT).show();
            } else if(supportsRssi == RssiSupport.UNKNOWN_OLDSERVICE)
            {
                // Toast.makeText(mContext, "Rssi might be supported. Please upgrade the plugin service.", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private MultiDeviceSearch.RssiCallback mRssiCallback = new MultiDeviceSearch.RssiCallback() {
        /**
         * Receive an RSSI data update from a specific found device
         */
        @Override
        public void onRssiUpdate(final int resultId, final int rssi) {
           /* runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (MultiDeviceSearchResultWithRSSI result : mFoundDevices)
                    {
                        if (result.mDevice.resultID == resultId)
                        {
                            result.mRSSI = rssi;
                            mFoundAdapter.notifyDataSetChanged();

                            break;
                        }
                    }
                }
            });*/
        }
    };
}
