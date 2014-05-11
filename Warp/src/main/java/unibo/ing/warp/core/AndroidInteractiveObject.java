package unibo.ing.warp.core;

import android.view.View;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by Lorenzo Donini on 5/11/2014.
 */
public class AndroidInteractiveObject implements InteractiveObject{
    private View mDeviceView;
    private IWarpDevice mWarpDevice;

    public AndroidInteractiveObject(IWarpDevice device)
    {
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
    public void updateView() {

    }
}
