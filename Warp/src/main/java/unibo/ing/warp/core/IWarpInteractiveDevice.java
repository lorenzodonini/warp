package unibo.ing.warp.core;

import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by cronic90 on 07/10/13.
 */
public interface IWarpInteractiveDevice {
    public IWarpDevice getWarpDevice();
    public Object getView();
    public void setView(Object view);
}
