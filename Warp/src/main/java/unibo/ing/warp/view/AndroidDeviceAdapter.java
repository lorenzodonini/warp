package unibo.ing.warp.view;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Toast;
import unibo.ing.warp.core.IWarpInteractiveDevice;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Lorenzo Donini on 5/13/2014.
 */
public class AndroidDeviceAdapter extends BaseAdapter implements IViewObserver {
    private List<IWarpInteractiveDevice> mDevices;
    private IWarpDeviceViewFactory mViewFactory;
    private boolean bNotifyOnChange;

    public AndroidDeviceAdapter(final Context context)
    {
        mDevices = new LinkedList<IWarpInteractiveDevice>();
        mViewFactory = new AndroidWarpDeviceViewFactory(context);
        bNotifyOnChange=true;
    }

    @Override
    public int getCount()
    {
        return mDevices.size();
    }

    @Override
    public IWarpInteractiveDevice getItem(int position)
    {
        return mDevices.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        IWarpInteractiveDevice device = getItem(position);
        if(device.getView() == null)
        {
            View view = (View) mViewFactory.createWarpDeviceView(device.getWarpDevice());
            if(view == null)
            {
                return null;
            }
            device.setView(view);
        }
        return (View)device.getView();
    }

    private void add(IWarpInteractiveDevice device)
    {
        if(device != null)
        {
            mDevices.add(device);
            if(bNotifyOnChange)
            {
                notifyDataSetChanged();
            }
        }
    }

    private void remove(IWarpInteractiveDevice device)
    {
        if(device != null)
        {
            mDevices.remove(device);
            if(bNotifyOnChange)
            {
                notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onWarpDeviceAdded(IWarpInteractiveDevice device)
    {
        add(device);
    }

    @Override
    public void onWarpDeviceRemoved(IWarpInteractiveDevice device)
    {
        remove(device);
    }

    @Override
    public void onWarpDeviceStatusChanged(IWarpInteractiveDevice device)
    {
        if(bNotifyOnChange)
        {
            notifyDataSetChanged();
        }
    }
}
