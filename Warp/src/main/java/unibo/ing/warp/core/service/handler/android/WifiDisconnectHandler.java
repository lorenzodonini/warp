package unibo.ing.warp.core.service.handler.android;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.service.handler.IWarpServiceHandler;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/22/2014.
 */
public final class WifiDisconnectHandler implements IWarpServiceHandler{
    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return new Object [] {(DefaultWarpInteractiveDevice)target};
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return null;
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
        //Do nothing
    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {
        //Do nothing
    }
}
