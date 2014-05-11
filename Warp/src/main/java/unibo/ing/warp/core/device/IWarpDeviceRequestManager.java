package unibo.ing.warp.core.device;

import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public interface IWarpDeviceRequestManager {
    public void onConnectRequest(IWarpDevice device, IWarpServiceListener listener);
    public void onDisconnectRequest(IWarpDevice device, IWarpServiceListener listener);
    public void onServicesLookupRequest(IWarpDevice device, IWarpServiceListener listener);
}
