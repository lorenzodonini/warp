package unibo.ing.warp.core.device;

import unibo.ing.warp.core.DefaultWarpInteractiveDevice;
import unibo.ing.warp.core.service.WarpServiceInfo;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

/**
 * Created by Lorenzo Donini on 5/10/2014.
 */
public interface IWarpDeviceRequestHandler {
    public void onConnectRequest(DefaultWarpInteractiveDevice device);
    public void onDisconnectRequest(DefaultWarpInteractiveDevice device);
    public void onPushServiceRequest(DefaultWarpInteractiveDevice device, WarpServiceInfo serviceDescriptor);
    public void onPullServiceRequest(DefaultWarpInteractiveDevice device, WarpServiceInfo serviceDescriptor);
    public void onServicesLookupRequest(DefaultWarpInteractiveDevice device, IWarpServiceListener listener);
}
