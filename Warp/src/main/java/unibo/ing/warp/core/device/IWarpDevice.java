package unibo.ing.warp.core.device;

import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.view.IWarpDeviceViewFactory;

import java.util.Collection;

/**
 * Created by cronic90 on 07/10/13.
 */
public interface IWarpDevice {
    public String getDeviceName();
    public String getDeviceInfo();
    public Object getAbstractDevice();
    public WarpLocation getDeviceLocation();
    public void updateAbstractDevice(Object abstractDevice);
}
