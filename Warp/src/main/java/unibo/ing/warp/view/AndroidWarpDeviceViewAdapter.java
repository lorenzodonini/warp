package unibo.ing.warp.view;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import unibo.ing.warp.R;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import unibo.ing.warp.core.device.IWarpDevice;

/**
 * Created by Lorenzo Donini on 5/18/2014.
 */
public class AndroidWarpDeviceViewAdapter implements IWarpDeviceViewAdapter {
    private Context mContext;

    public AndroidWarpDeviceViewAdapter(Context context)
    {
        mContext=context;
    }

    @Override
    public void adapt(Object view, int operationProgress)
    {
        if(view == null)
        {
            return;
        }
        View androidView = (View)view;
        ProgressBar progressBar = (ProgressBar) androidView.findViewById(R.id.deviceLoadingBar);
        progressBar.setProgress(operationProgress);
    }

    @Override
    public void adapt(Object view, String operationLabel)
    {
        //Do nothing for now
        /*if(view == null)
        {
            return;
        }
        View androidView = (View)view;
        //TextView labelView = (TextView) androidView.findViewById(R.id.deviceText);*/
    }

    @Override
    public void adapt(Object view, IWarpInteractiveDevice.WarpDeviceStatus status,
                      Class<? extends IWarpDevice> deviceClass)
    {
        if(view == null)
        {
            return;
        }
        View androidView = (View)view;
        ImageView iconView = (ImageView) androidView.findViewById(R.id.deviceIcon);
        iconView.setImageResource(AndroidWarpDeviceResourcesLibrary.getDrawableResourceId(status, deviceClass));
    }

    @Override
    public void adapt(Object view, int parameterKey, Object viewParameter)
    {
        //Still not implemented
    }
}
