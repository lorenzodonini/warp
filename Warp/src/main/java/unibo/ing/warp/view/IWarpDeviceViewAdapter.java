package unibo.ing.warp.view;

import unibo.ing.warp.core.IWarpInteractiveDevice.WarpDeviceStatus;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by Lorenzo Donini on 5/18/2014.
 */
public interface IWarpDeviceViewAdapter {
    public static final int VIEW_OPERATION_PROGRESS = 0;
    public static final int VIEW_OPERATION_LABEL = 1;

    public void adapt(Object view, int operationProgress);
    public void adapt(Object view, String operationLabel);
    public void adapt(Object view, WarpDeviceStatus status, Class<? extends IWarpDevice> deviceClass);
    public void adapt(Object view, int parameterKey, Object viewParameter);
}
