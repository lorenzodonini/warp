package unibo.ing.warp.core.device;

import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public abstract class DefaultWarpDevice implements IWarpDevice {
    public DefaultWarpDevice()
    {
    }

    @Override
    public synchronized void updateAbstractDevice(Object abstractDevice) {}

    public abstract Class<? extends IWarpService> getConnectServiceClass();
    public abstract Class<? extends IWarpService> getDisconnectServiceClass();
}
