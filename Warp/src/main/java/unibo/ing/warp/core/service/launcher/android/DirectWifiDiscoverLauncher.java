package unibo.ing.warp.core.service.launcher.android;

import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.launcher.DefaultWarpServiceLauncher;
import unibo.ing.warp.core.service.launcher.WarpResourceLibrary;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class DirectWifiDiscoverLauncher extends DefaultWarpServiceLauncher {
    //LISTENER
    private WarpAccessManager mAccessManager;
    //SERVICE
    private long mDiscoveryInterval;
    private WifiP2pManager.Channel mChannel;
    public static final String DISCOVERY_INTERVAL_KEY = "wifiDirectDiscoveryInterval";
    public static final String P2P_CHANNEL_KEY = "p2pChannel";

    @Override
    protected void initializeCallService(WarpResourceLibrary library, String permissionKey)
    {
        mAccessManager = (WarpAccessManager) library.getResource(
                permissionKey, WarpResourceLibrary.RES_ACCESS_MANAGER);
        mChannel = (WifiP2pManager.Channel) library.getResource(permissionKey, P2P_CHANNEL_KEY);
        mDiscoveryInterval = (Long) library.getResource(permissionKey, DISCOVERY_INTERVAL_KEY);
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
        return new Object[] {mAccessManager};
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
        return new Object[] {mDiscoveryInterval,mChannel};
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
