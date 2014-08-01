package unibo.ing.warp.core.service.launcher.android;

import android.content.Context;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class WifiScanLauncher extends DefaultWarpServiceLauncher {
    //LISTENER
    private WarpAccessManager mAccessManager;
    private WifiManager mWifiManager;
    //SERVICE
    private boolean bForceEnable;
    public static final String DISCOVER_INTERVAL_KEY = "wifiDiscoverInterval";
    public static final String WIFI_FORCE_ENABLE_KEY = "wifiForceEnable";

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        Context context = (Context) library.getResource(permissionKey,WarpResourceLibrary.RES_CONTEXT);
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mAccessManager = (WarpAccessManager) library.getResource(permissionKey,
                WarpResourceLibrary.RES_ACCESS_MANAGER);
        bForceEnable = (Boolean) library.getResource(permissionKey,WIFI_FORCE_ENABLE_KEY);
        //Not calling onServiceInitialized on purpose since this core service will be handled in a different way
    }

    @Override
    protected void initializeProvideService(WarpResourceLibrary library, String permissionKey)
    {
        //Do nothing
    }

    /*############ LISTENER PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return new Object [] {mAccessManager,mWifiManager};
    }

    @Override
    protected Object[] getProvideServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return null;
    }

    /*############ SERVICE PARAMETERS ############*/
    @Override
    protected Object[] getCallServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {bForceEnable};
    }

    @Override
    protected Object[] getProvideServiceParameters(IWarpInteractiveDevice target)
    {
        return null;
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }
}
