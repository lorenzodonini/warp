package unibo.ing.warp.core.device;

import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public interface IWarpDeviceRequestHandler {
    public void onConnectRequest(DefaultWarpDevice device, IWarpServiceListener listener);
    public void onDisconnectRequest(DefaultWarpDevice device, IWarpServiceListener listener);
    public void onServicesLookupRequest(DefaultWarpDevice device, IWarpServiceListener listener);
}
