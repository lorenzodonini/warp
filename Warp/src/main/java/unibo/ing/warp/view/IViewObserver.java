package unibo.ing.warp.view;

import unibo.ing.warp.core.IWarpInteractiveDevice;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public interface IViewObserver {
    public void onWarpDeviceStatusChanged(IWarpInteractiveDevice device);
    public void onWarpDeviceOperationProgressChanged(IWarpInteractiveDevice device);
    public void onWarpDeviceOperationLabelChanged(IWarpInteractiveDevice device);
}
