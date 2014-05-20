package unibo.ing.warp.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import unibo.ing.warp.R;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public class AndroidWarpDeviceViewFactory implements IWarpDeviceViewFactory {
    private Context mContext;

    public AndroidWarpDeviceViewFactory(Context context)
    {
        mContext=context;
    }

    @Override
    public Object createWarpDeviceView(IWarpDevice device, IWarpInteractiveDevice.WarpDeviceStatus status)
    {
        if(device == null)
        {
            return null;
        }
        if(status == IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED)
        {
            return createViewForDisconnectedDevice(device);
        }
        else if(status == IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED)
        {
            return createViewForConnectedDevice(device);
        }
        else if(status == IWarpInteractiveDevice.WarpDeviceStatus.FAILED)
        {
            return createViewForRefusedDevice(device);
        }
        else
        {
            return createViewForConnectingDevice(device);
        }
    }

    public View createViewForConnectedDevice(IWarpDevice device)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_view,null);
        if(view != null)
        {
            TextView deviceText = (TextView) view.findViewById(R.id.deviceName);
            deviceText.setText(device.getDeviceName());
            ImageView deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
            deviceIcon.setImageResource(AndroidWarpDeviceResourcesLibrary.getDrawableResourceId(
                    IWarpInteractiveDevice.WarpDeviceStatus.CONNECTED, device.getClass()));
        }
        return view;
    }

    public View createViewForDisconnectedDevice(IWarpDevice device)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_view,null);
        if(view != null)
        {
            TextView deviceText = (TextView) view.findViewById(R.id.deviceName);
            deviceText.setText(device.getDeviceName());
            ImageView deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
            deviceIcon.setImageResource(AndroidWarpDeviceResourcesLibrary.getDrawableResourceId(
                    IWarpInteractiveDevice.WarpDeviceStatus.DISCONNECTED, device.getClass()));
        }
        return view;
    }

    public View createViewForConnectingDevice(IWarpDevice device)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_view,null);
        if(view != null)
        {
            TextView deviceText = (TextView) view.findViewById(R.id.deviceName);
            deviceText.setText(device.getDeviceName());
            ImageView deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
            deviceIcon.setImageResource(AndroidWarpDeviceResourcesLibrary.getDrawableResourceId(
                    IWarpInteractiveDevice.WarpDeviceStatus.CONNECTING, device.getClass()));
        }
        return view;
    }

    public View createViewForRefusedDevice(IWarpDevice device)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.device_view,null);
        if(view != null)
        {
            TextView deviceText = (TextView) view.findViewById(R.id.deviceName);
            deviceText.setText(device.getDeviceName());
            ImageView deviceIcon = (ImageView) view.findViewById(R.id.deviceIcon);
            deviceIcon.setImageResource(AndroidWarpDeviceResourcesLibrary.getDrawableResourceId(
                    IWarpInteractiveDevice.WarpDeviceStatus.FAILED, device.getClass()));
        }
        return view;
    }
}
