package unibo.ing.warp.core.device.android;

import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class AndroidNetworkDevice extends DefaultWarpDevice {
    private WarpLocation mDeviceLocation;

    public AndroidNetworkDevice(WarpLocation location)
    {
        super();
        mDeviceLocation=location;
        //Once a device is discovered, it is virtually already connected
    }

    @Override
    public Class<? extends IWarpService> getConnectServiceClass() {
        return null;
    }

    @Override
    public Class<? extends IWarpService> getDisconnectServiceClass() {
        return null;
    }

    @Override
    public String getDeviceName()
    {
        /*CAREFUL: Don't call getHostAddress on the InetAddress object, otherwise a stupid
        Network operation is initiated, which leads to a crash if called on the UI thread!! */
        String result = mDeviceLocation.getStringIPv4Address();
        return result;
    }

    @Override
    public String getDeviceInfo()
    {
        return mDeviceLocation.getIPv4Address().getHostAddress();
    }

    @Override
    public Object getAbstractDevice()
    {
        return null;
    }

    @Override
    public WarpLocation getDeviceLocation()
    {
        return mDeviceLocation;
    }
}
