package unibo.ing.warp.view;

import unibo.ing.warp.core.IWarpInteractiveDevice;

/**
 * Created by Lorenzo Donini on 5/18/2014.
 */
public interface IViewLifecycleObserver extends IViewObserver {
    public void onWarpDeviceAdded(IWarpInteractiveDevice device);
    public void onWarpDeviceRemoved(IWarpInteractiveDevice device);
}
