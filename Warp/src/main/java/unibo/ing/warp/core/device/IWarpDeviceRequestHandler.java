package unibo.ing.warp.core.device;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public interface IWarpDeviceRequestHandler {
    public void onConnectRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener);
    public void onDisconnectRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener);
    public void onServicesLookupRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener);
}
