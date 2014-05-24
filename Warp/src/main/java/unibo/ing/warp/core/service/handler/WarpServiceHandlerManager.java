package unibo.ing.warp.core.service.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/21/2014.
 */
public class WarpServiceHandlerManager {
    private Map<String, IWarpServiceHandler> mServiceHandlers;

    public WarpServiceHandlerManager()
    {
        mServiceHandlers = new HashMap<String, IWarpServiceHandler>();
    }

    public void addServiceHandler(String serviceName, IWarpServiceHandler handler)
    {
        mServiceHandlers.put(serviceName,handler);
    }

    public IWarpServiceHandler getServiceHandlerByName(String serviceName)
    {
        return mServiceHandlers.get(serviceName);
    }
}
