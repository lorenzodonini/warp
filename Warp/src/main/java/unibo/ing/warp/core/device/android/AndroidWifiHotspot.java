package unibo.ing.warp.core.device.android;

import android.net.wifi.ScanResult;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.utils.WarpUtils;
import unibo.ing.warp.core.device.DefaultWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;

/**
 * Created by cronic90 on 07/10/13.
 */
public class AndroidWifiHotspot extends DefaultWarpDevice {
    private ScanResult mScanResult;

    public AndroidWifiHotspot(WarpAccessManager accessManager, ScanResult scanResult)
    {
        super(accessManager);
        mScanResult=scanResult;
    }

    @Override
    public String getDeviceName()
    {
        return (mScanResult!=null) ? mScanResult.SSID : null;
    }

    @Override
    public String getDeviceInfo()
    {
        return (mScanResult!=null) ? mScanResult.capabilities : null;
    }

    @Override
    public Object getAbstractDevice()
    {
        return mScanResult;
    }

    @Override
    public WarpLocation getDeviceLocation()
    {
        return null;
    }

    @Override
    public Class<? extends IWarpService> getConnectServiceClass()
    {
        return WifiConnectService.class;
    }

    @Override
    public Class<? extends IWarpService> getDisconnectServiceClass()
    {
        return null; //TODO: insert valid class
    }

    @Override
    public void connect(IWarpServiceListener listener)
    {
        getWarpRequestManager().onConnectRequest(this,listener);
    }

    @Override
    public void disconnect(IWarpServiceListener listener)
    {
        //TODO: to implement
    }

    @Override
    public synchronized void updateAbstractDevice(Object abstractDevice)
    {
        if(abstractDevice instanceof ScanResult)
        {
            mScanResult= (ScanResult) abstractDevice;
        }
    }
}
