package unibo.ing.warp.view;

import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public interface IWarpDeviceViewFactory {
    public Object createWarpDeviceView(Class<? extends IWarpDevice> deviceClass);
    public Object createWarpDeviceView(Class<? extends IWarpDevice> deviceClass, Object [] params);
}
