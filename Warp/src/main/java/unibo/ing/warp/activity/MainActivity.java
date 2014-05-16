package unibo.ing.warp.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import unibo.ing.warp.R;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidLocalDevice;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.base.PushFileService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.view.AndroidDeviceAdapter;
import java.util.Collection;

public class MainActivity extends Activity {
    private AndroidDeviceAdapter mListAdapter;
    private BroadcastReceiver mReceiver;
    private String mAccessKey = "TEMP_KEY";
    private static final long DEFAULT_DISCOVER_INTERVAL = 30000; //Every 30 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        //Creating Local Device
        manager.setLocalDevice(new AndroidLocalDevice(getApplicationContext(),null));
        //Adding Non-core Services
        final IWarpEngine warpDrive = manager.getLocalDevice().getWarpEngine();
        warpDrive.addWarpService(WifiScanService.class);
        warpDrive.addWarpService(WifiConnectService.class);
        warpDrive.addWarpService(DirectWifiDiscoverService.class);
        warpDrive.addWarpService(PushFileService.class);

        final WifiManager wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        //Graphics
        ToggleButton toggle=(ToggleButton)findViewById(R.id.discoveryToggle);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    IWarpServiceListener wifiScanListener = warpDrive.getDefaultListenerForService(
                            "scanService",new Object [] {manager,wifiManager});
                    warpDrive.callLocalService("scanService", wifiScanListener,
                            new Object[]{DEFAULT_DISCOVER_INTERVAL,true});
                    IWarpServiceListener directWifiListener = warpDrive.getDefaultListenerForService(
                            "p2pDiscovery", new Object [] {manager});
                    warpDrive.callLocalService("p2pDiscovery", directWifiListener,
                            new Object [] {DEFAULT_DISCOVER_INTERVAL+10000});
                }
                else
                {
                    Collection<IWarpService> activeServices = warpDrive.getActiveServices();
                    if(activeServices == null)
                    {
                        return;
                    }
                    for(IWarpService service : activeServices)
                    {
                        if(service instanceof WifiScanService)
                        {
                            ((WifiScanService)service).stopService();
                            break;
                        }
                    }
                }
            }
        });
        GridView gridView = (GridView)findViewById(R.id.deviceView);
        mListAdapter = new AndroidDeviceAdapter(this);
        gridView.setAdapter(mListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                IWarpInteractiveDevice device = mListAdapter.getItem(position);
                Toast.makeText(MainActivity.this,device.getWarpDevice().getDeviceName(),Toast.LENGTH_SHORT).show();
            }
        });
        manager.getDeviceManager().setViewObserver(mListAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}
