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
    public Collection<String> getAvailableServicesNames(IWarpServiceListener listener);
    public void setAvailableServicesNames(Collection<String> servicesNames);
    public boolean isConnected();
    public void setConnected(boolean connected);
    public void connect(IWarpServiceListener listener);
    public void disconnect(IWarpServiceListener listener);
    public void updateAbstractDevice(Object abstractDevice);
    public Class<? extends IWarpService> getConnectServiceClass();
    public Class<? extends IWarpService> getDisconnectServiceClass();
    public Object createView(IWarpDeviceViewFactory factory);
}
