package unibo.ing.warp.core;

import unibo.ing.warp.core.IBeam;
import unibo.ing.warp.core.service.IWarpService;

import java.io.IOException;

/**
 * Created by Lorenzo Donini on 5/2/2014.
 */
public interface IBeamHandler {
    public void onBeamCreate(IBeam warpBeam, IWarpService service);
    public void completeService(IBeam warpBeam, IWarpService servant);
    public void abortService(IBeam warpBeam, IWarpService servant, String message);
}
