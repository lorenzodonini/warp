package unibo.ing.warp.core.service.handler.android;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.handler.IWarpServiceHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public final class WifiConnectHandler implements IWarpServiceHandler {
    private WarpAccessManager mAccessManager;
    private WarpServiceInfo mChainedServiceToCall;
    public static final int CHAIN_SERVICE_CALL_KEY = 2;

    public WifiConnectHandler(WarpAccessManager manager)
    {
        mAccessManager = manager;
    }

    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return (mChainedServiceToCall != null) ? new Object[] {mAccessManager,target,mChainedServiceToCall}
                : new Object[] {mAccessManager,target};
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[] {target.getWarpDevice()};
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
        if(id == CHAIN_SERVICE_CALL_KEY)
        {
            mChainedServiceToCall = (WarpServiceInfo)param;
        }
    }

    @Override
    public void addServiceParameter(int id, Object param)
    {
        //Do nothing
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //Do nothing
    }
}
