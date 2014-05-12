package unibo.ing.warp.view;

import android.content.Context;
import android.widget.ImageView;
import unibo.ing.warp.core.device.IWarpDevice;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lorenzodonini on 12/05/14.
 */
public class AndroidWarpDeviceViewFactory implements IWarpDeviceViewFactory {
    private Map<Class<? extends IWarpDevice>, Integer> mResourceIdMapping;
    private Context mContext;

    public AndroidWarpDeviceViewFactory(Context context)
    {
        mContext=context;
        //TODO: populate the map somehow!!
        mResourceIdMapping = new HashMap<Class<? extends IWarpDevice>, Integer>();
    }

    @Override
    public Object createWarpDeviceView(Class<? extends IWarpDevice> deviceClass)
    {
        return null;
    }

    @Override
    public Object createWarpDeviceView(Class<? extends IWarpDevice> deviceClass, Object [] params)
    {
        if(deviceClass != null)
        {
            ImageView result = new ImageView(mContext);
            result.setImageResource(mResourceIdMapping.get(deviceClass));
            //TODO: check if it works!!
            return result;
        }
        return null;
    }
}
