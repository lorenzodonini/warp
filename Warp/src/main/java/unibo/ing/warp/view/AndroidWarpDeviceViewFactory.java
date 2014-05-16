package unibo.ing.warp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import unibo.ing.warp.R;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.android.AndroidP2PDevice;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public class AndroidWarpDeviceViewFactory implements IWarpDeviceViewFactory {
    private Map<Class<? extends IWarpDevice>, Integer> mResourceIdMapping;
    private Context mContext;

    public AndroidWarpDeviceViewFactory(Context context)
    {
        mContext=context;
        //TODO: populate the map somehow!!
        mResourceIdMapping = new HashMap<Class<? extends IWarpDevice>, Integer>();
        mResourceIdMapping.put(AndroidWifiHotspot.class,R.drawable.wifi_hotspot);
        mResourceIdMapping.put(AndroidP2PDevice.class,R.drawable.p2p_device);
    }

    @Override
    public Object createWarpDeviceView(IWarpDevice device)
    {
        if(device != null)
        {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.device_view,null);
            if(view != null)
            {
                TextView deviceText = (TextView) view.findViewById(R.id.deviceText);
                deviceText.setText(device.getDeviceName());

                ImageView deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
                if(device.isConnected())
                {
                    deviceIcon.setImageResource(R.drawable.wifi_hotspot_connected);
                }
                else
                {
                    deviceIcon.setImageResource(mResourceIdMapping.get(device.getClass()));
                }
            }
            return view;
        }
        return null;
    }
}
