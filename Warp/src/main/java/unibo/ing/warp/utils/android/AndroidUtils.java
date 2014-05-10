package unibo.ing.warp.utils.android;

import android.os.AsyncTask;
import unibo.ing.warp.core.WarpLocation;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class AndroidUtils {
    public static void getIpv4Address(Object address, IAndroidInetAddressListener listener)
    {
        new IPv4RetrievalTask(listener).execute(address);
    }

    private static class IPv4RetrievalTask extends AsyncTask<Object, Void, InetAddress> {
        private IAndroidInetAddressListener mListener;

        public IPv4RetrievalTask(IAndroidInetAddressListener listener)
        {
            mListener=listener;
        }
        @Override
        protected InetAddress doInBackground(Object... params)
        {
            try{
                if(params.length == 0)
                {
                    return InetAddress.getLocalHost();
                }
                if(params[0] instanceof byte [])
                {
                    byte [] addr = (byte[]) params[0];
                    return InetAddress.getByAddress(addr);
                }
                else if(params[0] instanceof String)
                {
                    String addr = (String)params[0];
                    return InetAddress.getByName(addr);
                }
                else
                {
                    return InetAddress.getLocalHost();
                }
            }
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(InetAddress result)
        {
            if(mListener!=null)
            {
                mListener.onAddressRetrieved(result);
            }
        }
    }

    public interface IAndroidInetAddressListener {
        public void onAddressRetrieved(InetAddress address);
    }
}
