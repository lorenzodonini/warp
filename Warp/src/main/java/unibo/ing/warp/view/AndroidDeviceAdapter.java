package unibo.ing.warp.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import unibo.ing.warp.core.IWarpInteractiveDevice;

/**
 * Created by lorenzodonini on 11/05/14.
 */
public class AndroidDeviceAdapter extends ArrayAdapter<IWarpInteractiveDevice> implements IViewObserver {
    private IWarpDeviceViewFactory mViewFactory;

    public AndroidDeviceAdapter(Context context, int resource)
    {
        super(context, resource);
        mViewFactory = new AndroidWarpDeviceViewFactory(getContext());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView != null)
        {
            return convertView;
        }
        IWarpInteractiveDevice device = getItem(position);
        if(device.getView() == null)
        {
            device.setView(mViewFactory.createWarpDeviceView(device.getWarpDevice().getClass()));
        }
        return (View)device.getView();
    }

    @Override
    public void onWarpDeviceAdded(IWarpInteractiveDevice device)
    {
        //TODO: implement
    }

    @Override
    public void onWarpDeviceRemoved(IWarpInteractiveDevice device)
    {
        //TODO: implement
    }
}
