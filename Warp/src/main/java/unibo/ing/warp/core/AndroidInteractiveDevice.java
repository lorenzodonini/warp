package unibo.ing.warp.core;

import android.view.View;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.IWarpDeviceRequestHandler;

/**
 * Created by Lorenzo Donini on 5/11/2014.
 */
public class AndroidInteractiveDevice extends DefaultWarpInteractiveDevice {
    private View mDeviceView;
    private IWarpDevice mWarpDevice;

    public AndroidInteractiveDevice(IWarpDeviceRequestHandler requestManager, IWarpDevice device)
    {
        super(requestManager);
        mWarpDevice = device;
    }

    public AndroidInteractiveDevice(IWarpDeviceRequestHandler requestManager, WarpDeviceStatus status,
                                    IWarpDevice device)
    {
        super(requestManager,status);
        mWarpDevice = device;
    }

    @Override
    public IWarpDevice getWarpDevice()
    {
        return mWarpDevice;
    }

    @Override
    public Object getView()
    {
        return mDeviceView;
    }

    @Override
    public void setView(Object view)
    {
        mDeviceView=(View)view;
    }
}
