package unibo.ing.warp.core.service.handler;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public final class LookupServiceResourcesHandler implements IWarpServiceResourcesHandler {
    @Override
    public Object[] getServiceListenerParameters(IWarpInteractiveDevice target)
    {
        return new Object[0];
    }

    @Override
    public Object[] getServiceParameters(IWarpInteractiveDevice target)
    {
        return new Object[0];
    }

    @Override
    public IWarpable[] getServiceRemoteParameters()
    {
        return new IWarpable[0];
    }

    @Override
    public void addServiceListenerParameter(int id, Object param)
    {

    }

    @Override
    public void addServiceParameter(int id, Object param)
    {

    }

    @Override
    public void addServiceRemoteParameter(int id, IWarpable param)
    {

    }
}
