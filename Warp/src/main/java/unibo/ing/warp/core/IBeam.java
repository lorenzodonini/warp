package unibo.ing.warp.core;

import org.json.JSONException;
import unibo.ing.warp.core.warpable.IWarpable;

import java.io.IOException;
import java.util.EnumSet;

/**
 * User: lorenzodonini
 * Date: 15/11/13
 * Time: 15:39
 */
public interface IBeam {
    public int beamWarpable(IWarpable object) throws IOException, JSONException;
    public int receiveWarpable(IWarpable result) throws IOException, JSONException;
    public WarpLocation getPeerWarpLocation();
    public WarpFlag getFlag(String propertyName);
    public WarpFlag [] getFlags();
    public void addFlag(WarpFlag flag);
    public void removeFlag(String propertyName);
    public void disable() throws IOException;
    public boolean isEnabled();
}
