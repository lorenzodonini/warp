package unibo.ing.warp.core.service.listener;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public interface IWarpServiceListenerFactory {
    public DefaultWarpServiceListener createWarpServiceListener(
                    String serviceName, Object [] params);
    public void addWarpServiceListenerMapping(String serviceName,
                    Class<? extends DefaultWarpServiceListener> listenerClass);
}
