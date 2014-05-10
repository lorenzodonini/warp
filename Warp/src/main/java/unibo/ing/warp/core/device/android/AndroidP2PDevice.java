package unibo.ing.warp.core.device.android;

import android.net.wifi.p2p.WifiP2pDevice;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * User: lorenzodonini
 * Date: 24/11/13
 * Time: 15:19
 */
public class AndroidP2PDevice extends DefaultWarpDevice {
    private WifiP2pDevice mP2pDevice;
    private WarpLocation mWarpLocation;

    public AndroidP2PDevice(WarpAccessManager accessManager, WarpLocation location, WifiP2pDevice device)
    {
        super(accessManager);
        mP2pDevice=device;
        mWarpLocation = location;
    }

    @Override
    public String getDeviceName()
    {
        return (mP2pDevice != null) ? mP2pDevice.deviceName : null;
    }

    @Override
    public String getDeviceInfo()
    {
        return (mP2pDevice != null) ? mP2pDevice.primaryDeviceType : null;
    }

    @Override
    public Object getAbstractDevice()
    {
        return mP2pDevice;
    }

    @Override
    public WarpLocation getDeviceLocation()
    {
        return mWarpLocation;
    }

    @Override
    public void connect(IWarpServiceListener listener)
    {
        //TODO: to implement
    }

    @Override
    public void disconnect(IWarpServiceListener listener)
    {
        //TODO: to implement
    }

    @Override
    public Class<? extends IWarpService> getConnectServiceClass()
    {
        return null;
    }

    @Override
    public Class<? extends IWarpService> getDisconnectServiceClass()
    {
        return null;
    }
}
