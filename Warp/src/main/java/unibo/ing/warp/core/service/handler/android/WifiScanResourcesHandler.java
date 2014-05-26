package unibo.ing.warp.core.service.handler.android;

import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class WifiScanResourcesHandler implements IWarpServiceResourcesHandler {
    //LISTENER
    private WarpAccessManager mAccessManager;
    private WifiManager mWifiManager;
    //SERVICE
    private long mDiscoverInterval;
    private boolean bForceEnable;
    public static final int DISCOVER_INTERVAL_KEY = 0;
    public static final int WIFI_FORCE_ENABLE_KEY = 1;

    public WifiScanResourcesHandler(WarpAccessManager accessManager, WifiManager wifiManager)
    {
        mAccessManager=accessManager;
        mWifiManager=wifiManager;
    }
    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return new Object [] {mAccessManager,mWifiManager};
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {mDiscoverInterval,bForceEnable};
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }

    @Override
    public void addServiceListenerParameter(int id, Object param)
    {
        //Do nothing
    }

    @Override
    public void addServiceParameter(int id, Object param)
    {
        if(id == DISCOVER_INTERVAL_KEY)
        {
            mDiscoverInterval = (Long)param;
        }
        else if(id == WIFI_FORCE_ENABLE_KEY)
        {
            bForceEnable = (Boolean)param;
        }
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //Do nothing
    }
}
