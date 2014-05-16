package unibo.ing.warp.core.android;

import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.core.service.base.PushFileService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.service.listener.android.AndroidDirectWifiDiscoverServiceListener;
import unibo.ing.warp.core.service.listener.android.AndroidPushFileServiceListener;
import unibo.ing.warp.core.service.listener.android.AndroidWifiScanServiceListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/8/2014.
 */
public class AndroidListenerMapping {
    private static Map<Class<? extends IWarpService>, Class<? extends DefaultWarpServiceListener>> listenerMapping;

    static {
        listenerMapping = new HashMap<Class<? extends IWarpService>,
                Class<? extends DefaultWarpServiceListener>>();
        listenerMapping.put(PushFileService.class, AndroidPushFileServiceListener.class);
        listenerMapping.put(WifiScanService.class, AndroidWifiScanServiceListener.class);
        listenerMapping.put(DirectWifiDiscoverService.class, AndroidDirectWifiDiscoverServiceListener.class);
    }

    public static Class<? extends DefaultWarpServiceListener> getListenerClass(
            Class<? extends IWarpService> serviceClass)
    {
        return listenerMapping.get(serviceClass);
    }
}
