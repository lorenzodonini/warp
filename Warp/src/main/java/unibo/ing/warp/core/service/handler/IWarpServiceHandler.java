package unibo.ing.warp.core.service.handler;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.warpable.IWarpable;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public interface IWarpServiceHandler {
    public Object [] getServiceListenerParameters(IWarpInteractiveDevice target);
    public Object [] getServiceParameters(IWarpInteractiveDevice target);
    public IWarpable [] getServiceRemoteParameters();
    public void addServiceListenerParameter(int id, Object param);
    public void addServiceParameter(int id, Object param);
    public void addServiceRemoteParameter(int id, IWarpable param);
}
