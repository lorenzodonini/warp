package unibo.ing.warp.core.service.listener;

/**
 * Created by Lorenzo Donini on 5/8/2014.
 */
public abstract class DefaultWarpServiceListener implements IWarpServiceListener {
    public DefaultWarpServiceListener() {}
    public abstract void putDefaultValues(Object [] values);
}
