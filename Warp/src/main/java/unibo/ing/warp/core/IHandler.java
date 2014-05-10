package unibo.ing.warp.core;

import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by Lorenzo Donini on 4/17/2014.
 */
public interface IHandler {
    public void onServiceProgressUpdate(IWarpService servant);
    public void onServiceStatusChanged(IWarpService servant);
    public IWarpService.ServiceStatus getHandledServiceStatus();
}
