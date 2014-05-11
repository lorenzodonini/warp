package unibo.ing.warp.activity;

import android.net.wifi.ScanResult;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.*;
import unibo.ing.warp.R;
import unibo.ing.warp.core.IWarpEngine;
import unibo.ing.warp.core.WarpLocation;
import unibo.ing.warp.core.device.IWarpDevice;
import unibo.ing.warp.core.device.WarpAccessManager;
import unibo.ing.warp.core.device.android.AndroidLocalDevice;
import unibo.ing.warp.core.device.WarpDeviceManager;
import unibo.ing.warp.core.device.android.AndroidWifiHotspot;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.base.PushFileService;
import unibo.ing.warp.core.service.listener.IWarpServiceListener;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends Activity {
    private ArrayAdapter<String> mListAdapter;
    private IWarpServiceListener wifiScanListener;
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
        warpDrive.addWarpService(PushFileService.class);

        //Graphics
        ToggleButton toggle=(ToggleButton)findViewById(R.id.discoveryToggle);
        initializeLocalListeners();
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    warpDrive.callLocalService("scanService", wifiScanListener,
                            new Object[]{DEFAULT_DISCOVER_INTERVAL,true});
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
        ListView listView=(ListView)findViewById(R.id.deviceList);
        mListAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
        listView.setAdapter(mListAdapter);
        //TODO: Add onClick listener for adapter items
    }

    private void initializeLocalListeners()
    {
        wifiScanListener=new IWarpServiceListener() {
            @Override
            public void onServiceCompleted(IWarpService servant)
            {
                Toast.makeText(MainActivity.this,"Terminated!",Toast.LENGTH_SHORT).show();
                updateWifiDevices(servant);
            }

            @Override
            public void onServiceProgressUpdate(IWarpService servant)
            {
                Toast.makeText(MainActivity.this,"Results found!",Toast.LENGTH_SHORT).show();
                updateWifiDevices(servant);
            }

            @Override
            public void onServiceAbort(String message)
            {
                Toast.makeText(MainActivity.this,message,Toast.LENGTH_LONG).show();
            }
        };
    }

    private void updateWifiDevices(IWarpService servant)
    {
        List<ScanResult> scanResults = (List<ScanResult>) servant.getResult()[0];
        IWarpDevice devices [] = new IWarpDevice[scanResults.size()];
        Iterator<ScanResult> iterator = scanResults.iterator();
        WarpAccessManager manager = WarpAccessManager.getInstance(mAccessKey);
        int i=0;
        while(i<devices.length && iterator.hasNext())
        {
            devices[i]=new AndroidWifiHotspot(manager,iterator.next());
        }
        WarpDeviceManager deviceManager = manager.getDeviceManager();
        deviceManager.addHomogeneousWarpDeviceCollection(devices,AndroidWifiHotspot.class,true);
        mListAdapter.clear();
        mListAdapter.addAll(deviceManager.getWarpDevicesNames());
        mListAdapter.notifyDataSetChanged();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
