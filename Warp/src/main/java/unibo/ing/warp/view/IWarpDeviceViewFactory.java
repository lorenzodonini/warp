package unibo.ing.warp.view;

import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public interface IWarpDeviceViewFactory {
    //TODO: may wanna refactor it into IWarpDevice as a parameter instead of the class
    public Object createWarpDeviceView(IWarpDevice device, IWarpInteractiveDevice.WarpDeviceStatus status);
}
