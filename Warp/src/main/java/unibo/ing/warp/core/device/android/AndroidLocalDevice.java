package unibo.ing.warp.core.device.android;

import android.content.Context;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.utils.android.AndroidUtils;
import unibo.ing.warp.core.android.AndroidWarpDrive;
import unibo.ing.warp.core.device.IWarpLocalDevice;
import unibo.ing.warp.core.service.base.LookupService;
import unibo.ing.warp.core.service.base.PushObjectService;
import java.net.InetAddress;

/**
 * User: lorenzodonini
 * Date: 24/11/13
 * Time: 14:56
 */
public class AndroidLocalDevice implements IWarpLocalDevice {
    private IWarpEngine mWarpDrive;
    private WarpLocation mDriveLocation;

    public AndroidLocalDevice(Context context,WarpLocation location)
    {
        mWarpDrive=new AndroidWarpDrive(context);
        //Adding Local Services
        mWarpDrive.addWarpService(PushObjectService.class);
        mWarpDrive.addWarpService(LookupService.class);
        /*mWarpDrive.addWarpService(new ConnectionSetupService());
        mWarpDrive.addWarpService(new WifiConnectService());
        mWarpDrive.addWarpService(new WifiScanService());*/
        if(location != null && location.getIPv4Address() != null)
        {
            mDriveLocation = location;
        }
        else
        {
            //TODO: not exactly necessary anymore! check it out
            AndroidUtils.getIpv4Address(WarpLocation.LOCAL_ADDRESS,new AndroidUtils.IAndroidInetAddressListener() {
                @Override
                public void onAddressRetrieved(InetAddress address) {
                    mDriveLocation = new WarpLocation();
                    mDriveLocation.setIPv4Address(address);
                    mWarpDrive.startEngine();
                }
            });
        }
    }
    @Override
    public String getDeviceName()
    {
        return null;
    }

    @Override
    public String getDeviceInfo()
    {
        return null;
    }

    @Override
    public WarpLocation getDeviceLocation()
    {
        return mDriveLocation;
    }

    @Override
    public IWarpEngine getWarpEngine()
    {
        return mWarpDrive;
    }
}
