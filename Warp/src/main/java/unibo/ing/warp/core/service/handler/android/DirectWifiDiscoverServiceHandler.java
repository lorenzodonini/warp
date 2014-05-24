package unibo.ing.warp.core.service.handler.android;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.handler.IWarpServiceHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class DirectWifiDiscoverServiceHandler implements IWarpServiceHandler {
    private long mDiscoveryInterval;
    private WarpAccessManager mAccessManager;
    public static final int DISCOVERY_INTERVAL_KEY = 0;

    public DirectWifiDiscoverServiceHandler(WarpAccessManager manager)
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
        return new Object[] {mDiscoveryInterval};
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
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //Do nothing
    }
}
