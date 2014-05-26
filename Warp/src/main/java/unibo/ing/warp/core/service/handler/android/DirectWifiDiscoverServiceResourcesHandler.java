package unibo.ing.warp.core.service.handler.android;

import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class DirectWifiDiscoverServiceResourcesHandler implements IWarpServiceResourcesHandler {
    private long mDiscoveryInterval;
    private WarpAccessManager mAccessManager;
    private WifiP2pManager.Channel mChannel;
    public static final int DISCOVERY_INTERVAL_KEY = 0;
    public static final int P2P_CHANNEL = 1;

    public DirectWifiDiscoverServiceResourcesHandler(WarpAccessManager manager)
    {
        mAccessManager=manager;
    }

    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {mAccessManager};
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {mDiscoveryInterval,mChannel};
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
        if(id == DISCOVERY_INTERVAL_KEY)
        {
            mDiscoveryInterval = (Long)param;
        }
        else if(id == P2P_CHANNEL)
        {
            mChannel = (WifiP2pManager.Channel)param;
        }
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //Do nothing
    }
}
