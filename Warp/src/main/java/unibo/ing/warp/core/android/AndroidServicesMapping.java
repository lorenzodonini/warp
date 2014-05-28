package unibo.ing.warp.core.android;

import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiConnectService;
import unibo.ing.warp.core.service.android.p2p.DirectWifiDiscoverService;
import unibo.ing.warp.core.service.android.wifi.WifiConnectService;
import unibo.ing.warp.core.service.android.wifi.WifiDisconnectService;
import unibo.ing.warp.core.service.android.wifi.WifiScanService;
import unibo.ing.warp.core.service.base.PushFileService;
import unibo.ing.warp.core.service.handler.IWarpServiceResourcesHandler;
import unibo.ing.warp.core.service.handler.android.WifiDisconnectResourcesHandler;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.service.listener.android.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/8/2014.
 */
public class AndroidServicesMapping {
    private static Map<Class<? extends IWarpService>, Class<? extends DefaultWarpServiceListener>> listenerMapping;
    private static Map<Class<? extends IWarpService>, Class<? extends IWarpServiceResourcesHandler>> handlerMapping;

    static {
        listenerMapping = new HashMap<Class<? extends IWarpService>,
                Class<? extends DefaultWarpServiceListener>>();
        listenerMapping.put(PushFileService.class, AndroidPushFileServiceListener.class);
        listenerMapping.put(WifiScanService.class, AndroidWifiScanServiceListener.class);
        listenerMapping.put(DirectWifiDiscoverService.class, AndroidDirectWifiDiscoverServiceListener.class);
        listenerMapping.put(WifiConnectService.class, AndroidWifiConnectServiceListener.class);
        listenerMapping.put(WifiDisconnectService.class, AndroidWifiDisconnectServiceListener.class);
        listenerMapping.put(DirectWifiConnectService.class, AndroidDirectWifiConnectServiceListener.class);

        handlerMapping = new HashMap<Class<? extends IWarpService>, Class<? extends IWarpServiceResourcesHandler>>();
        handlerMapping.put(WifiDisconnectService.class, WifiDisconnectResourcesHandler.class);
        //TODO: implement the rest
    }

    public static Class<? extends DefaultWarpServiceListener> getListenerClass(
            Class<? extends IWarpService> serviceClass)
    {
        return listenerMapping.get(serviceClass);
    }

    public static Class<? extends IWarpServiceResourcesHandler> getHandlerClass(Class<? extends IWarpService> serviceClass)
    {
        return handlerMapping.get(serviceClass);
    }
}
