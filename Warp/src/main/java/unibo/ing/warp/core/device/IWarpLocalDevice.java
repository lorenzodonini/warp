package unibo.ing.warp.core.device;

import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;

/**
 * Created by Lorenzo Donini on 5/6/2014.
 */
public interface IWarpLocalDevice {
    public String getDeviceName();
    public String getDeviceInfo();
    public IWarpEngine getWarpEngine();
    public WarpLocation getDeviceLocation();
}
