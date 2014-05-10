package unibo.ing.warp.core.device.android;

import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class AndroidNetworkDevice extends DefaultWarpDevice {
    private WarpLocation mDeviceLocation;

    public AndroidNetworkDevice(WarpAccessManager accessManager, WarpLocation location)
    {
        super(accessManager);
        mDeviceLocation=location;
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
        return mDeviceLocation.getIPv4Address().getHostName();
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

    @Override
    public void connect(IWarpServiceListener listener)
    {
        //Inside a LAN nothing needs to be implemented, since the devices are already in visibility
        if(listener!=null)
        {
            listener.onServiceCompleted(null);
        }
    }

    @Override
    public void disconnect(IWarpServiceListener listener) {
        //TODO: to implement
    }
}
