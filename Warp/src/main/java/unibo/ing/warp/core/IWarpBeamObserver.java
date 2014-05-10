package unibo.ing.warp.core;

import unibo.ing.warp.core.service.IWarpService;

/**
 * Created by Lorenzo Donini on 5/1/2014.
 */
public interface IWarpBeamObserver {
    public void onBeamCreate(IBeam warpBeam, IWarpService service);
    public void onBeamDestroy(IBeam warpBeam, IWarpService service);
}
