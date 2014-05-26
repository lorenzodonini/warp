package unibo.ing.warp.core.service.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public class WarpServiceResourcesHandlerManager {
    private Map<String, IWarpServiceResourcesHandler> mServiceHandlers;

    public WarpServiceResourcesHandlerManager()
    {
        mServiceHandlers = new HashMap<String, IWarpServiceResourcesHandler>();
    }

    public void addServiceHandler(String serviceName, IWarpServiceResourcesHandler handler)
    {
        mServiceHandlers.put(serviceName,handler);
    }

    public IWarpServiceResourcesHandler getServiceHandlerByName(String serviceName)
    {
        return mServiceHandlers.get(serviceName);
    }
}
