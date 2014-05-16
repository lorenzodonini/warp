package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import java.util.Collection;

/**
 * Created by Lorenzo Donini on 5/16/2014.
 */
public class AndroidNetworkStateManager extends BroadcastReceiver {
    private WarpAccessManager mWarpAccessManager;
    private WifiManager mWifiManager;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        String action = intent.getAction();
        if(action == null)
        {
            return;
        }
        if(action.equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION))
        {
            boolean connected=intent.getBooleanExtra(WifiManager.EXTRA_SUPPLICANT_CONNECTED,false);
            WifiInfo info = mWifiManager.getConnectionInfo();
            Collection<IWarpInteractiveDevice> devices = mWarpAccessManager.
                    getDeviceManager().getInteractiveDevices();
            for(IWarpInteractiveDevice device : devices)
            {
                if(("\""+device.getWarpDevice().getDeviceName()+"\"").equals(info.getSSID()))
                {
                    device.getWarpDevice().setConnected(true);
                    device.setView(null);
                    return;
                }
            }
        }
    }
}
