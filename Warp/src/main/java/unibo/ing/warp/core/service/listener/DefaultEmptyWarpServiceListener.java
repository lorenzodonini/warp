package unibo.ing.warp.core.service.listener;

import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by Lorenzo Donini on 6/7/2014.
 */
public class DefaultEmptyWarpServiceListener extends DefaultWarpServiceListener {
    @Override
    public void putDefaultValues(Object[] values) {}

    @Override
    public void onServiceCompleted(IWarpService servant) {}

    @Override
    public void onServiceProgressUpdate(IWarpService servant) {}

    @Override
    public void onServiceAbort(String message) {}
}
