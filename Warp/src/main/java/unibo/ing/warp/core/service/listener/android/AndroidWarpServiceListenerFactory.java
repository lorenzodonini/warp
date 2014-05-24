package unibo.ing.warp.core.service.listener.android;

import android.util.Log;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import unibo.ing.warp.core.service.listener.IWarpServiceListenerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lorenzo Donini on 5/8/2014.
 */
public class AndroidWarpServiceListenerFactory implements IWarpServiceListenerFactory {
    private Map<String, Class<? extends DefaultWarpServiceListener>> mListenerMap;

    public AndroidWarpServiceListenerFactory()
    {
        mListenerMap = new HashMap<String, Class<? extends DefaultWarpServiceListener>>();
    }

    @Override
    public DefaultWarpServiceListener createWarpServiceListener(String serviceName, Object[] params)
    {
        Class<? extends DefaultWarpServiceListener> listenerClass = mListenerMap.get(serviceName);
        DefaultWarpServiceListener listener = null;
        if(listenerClass != null)
        {
            try {
                listener = listenerClass.newInstance();
                listener.putDefaultValues(params);
            }
            catch (Exception e)
            {
                Log.e("AndroidWarpServiceListenerFactory.createWarpServiceListener",
                        "Couldn't instantiate "+listenerClass);
            }
        }
        return listener;
    }

    @Override
    public void addWarpServiceListenerMapping(String serviceName,
                                              Class<? extends DefaultWarpServiceListener> listenerClass)
    {
        mListenerMap.put(serviceName,listenerClass);
    }
}
