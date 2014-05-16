package unibo.ing.warp.core.service.android.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.util.Log;
import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.DefaultWarpService;
import unibo.ing.warp.core.service.WarpServiceInfo;

/**
 * User: lorenzodonini
 * Date: 10/11/13
 * Time: 22:03
 */
@WarpServiceInfo(type= WarpServiceInfo.Type.LOCAL,name="scanService",
        target = WarpServiceInfo.Target.ANDROID, execution = WarpServiceInfo.ServiceExecution.CONCURRENT)
public class WifiScanService extends DefaultWarpService {
    private boolean bEnabled=false;
    private BroadcastReceiver mReceiver;

    @Override
    public void callService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        checkOptionalParameters(params,2);
        Context androidContext = (Context)context;
        long discoverInterval = (Long)params[0];
        boolean forceEnable = (Boolean)params[1];

        setContext(androidContext);
        mReceiver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onScanResultsAvailable();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        androidContext.registerReceiver(mReceiver,filter);
        setEnabled(true);
        scanOperation(discoverInterval, forceEnable);
    }

    @Override
    public void provideService(IBeam warpBeam, Object context, Object[] params) throws Exception
    {
        //DO NOTHING SINCE THIS IS A LOCAL SERVICE
    }

    private void scanOperation(long discoverInterval, boolean forceEnable) throws Exception
    {
        Context context = (Context)getContext();
        WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(forceEnable)
        {
            //We can forcefully enable WiFi
            manager.setWifiEnabled(true);
        }
        while (isEnabled())
        {
            manager.startScan();
            Thread.sleep(discoverInterval);
        }
    }

    private synchronized boolean isEnabled()
    {
        return bEnabled;
    }

    private synchronized void setEnabled(boolean enabled)
    {
        bEnabled=enabled;
        if(!enabled && mReceiver != null)
        {
            Context context = (Context) getContext();
            context.unregisterReceiver(mReceiver);
        }
    }

    public void stopService()
    {
        bEnabled=false;
    }

    private void onScanResultsAvailable()
    {
        if(isEnabled())
        {
            getWarpServiceHandler().onServiceProgressUpdate(this);
        }
    }

    @Override
    public Object[] getResult()
    {
        return getCurrentProgress();
    }

    @Override
    public Object[] getCurrentProgress()
    {
        WifiManager manager = (WifiManager)((Context)getContext()).getSystemService(Context.WIFI_SERVICE);
        return new Object [] {manager.getScanResults()};
    }
}
