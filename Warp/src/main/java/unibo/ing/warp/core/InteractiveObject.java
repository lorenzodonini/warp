package unibo.ing.warp.core;

import android.view.View;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by cronic90 on 07/10/13.
 */
public interface InteractiveObject {
    public IWarpDevice getWarpDevice();
    public View getView();
}
