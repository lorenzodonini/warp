package unibo.ing.warp.core.service.handler.android;

import android.net.wifi.p2p.WifiP2pManager;
import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by lorenzodonini on 26/05/14.
 */
public final class DirectWifiConnectServiceResourcesHandler implements IWarpServiceResourcesHandler {
    private WifiP2pManager.Channel mChannel;
    private WarpAccessManager accessManager;
    private WarpServiceInfo chainServiceToCall;
    private int groupOwnerPriority=1;
    public static final int P2P_CHANNEL_KEY = 1;
    public static final int P2P_OWNER_PRIORITY_KEY = 2;
    public static final int CHAIN_SERVICE_CALL_KEY = 2;

    public DirectWifiConnectServiceResourcesHandler(WarpAccessManager manager)
    {
        accessManager=manager;
    }

    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return (chainServiceToCall != null) ? new Object[] {accessManager,(DefaultWarpInteractiveDevice)
                target,chainServiceToCall} : new Object[] {accessManager,(DefaultWarpInteractiveDevice)target};
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {target.getWarpDevice(),mChannel,groupOwnerPriority};
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return null;
    }

    @Override
    public void addServiceListenerParameter(int id, Object param)
    {
        if(id == CHAIN_SERVICE_CALL_KEY)
        {
            chainServiceToCall = (WarpServiceInfo)param;
        }
    }

    @Override
    public void addServiceParameter(int id, Object param)
    {
        if(id == P2P_CHANNEL_KEY)
        {
            mChannel=(WifiP2pManager.Channel)param;
        }
        else if(id == P2P_OWNER_PRIORITY_KEY)
        {
            groupOwnerPriority = (Integer)param;
        }
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //DO nothing
    }
}
