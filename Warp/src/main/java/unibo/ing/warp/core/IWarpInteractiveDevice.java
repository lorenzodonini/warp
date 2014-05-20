package unibo.ing.warp.core;

import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;

import java.util.Collection;

/**
 * Created by cronic90 on 07/10/13.
 */
public interface IWarpInteractiveDevice {
    public IWarpDevice getWarpDevice();
    public Object getView();
    public void setView(Object view);
    public void connect(IWarpServiceListener listener);
    public void disconnect(IWarpServiceListener listener);
    public Collection<String> getAvailableServicesNames(IWarpServiceListener listener);
    public void addAvailableServicesNames(Collection<String> servicesNames);
    public WarpDeviceStatus getDeviceStatus();
    public int getDeviceOperationProgress();
    public String getDeviceOperationLabel();
    public void updateDeviceData(IWarpInteractiveDevice newDevice);

    public enum WarpDeviceStatus {
        DISCONNECTED, CONNECTING, CONNECTED, FAILED
    }
}
