package unibo.ing.warp.view;

import unibo.ing.warp.core.IWarpInteractiveDevice;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public interface IViewObserver {
    public void onWarpDeviceAdded(IWarpInteractiveDevice device);
    public void onWarpDeviceRemoved(IWarpInteractiveDevice device);
    public void onWarpDeviceStatusChanged(IWarpInteractiveDevice device);
}
