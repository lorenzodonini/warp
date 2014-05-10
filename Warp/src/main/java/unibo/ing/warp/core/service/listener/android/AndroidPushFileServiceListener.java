package unibo.ing.warp.core.service.listener.android;

import android.os.Environment;
import unibo.ing.warp.core.service.IWarpService;
import unibo.ing.warp.core.service.listener.DefaultWarpServiceListener;
import java.io.File;
import java.io.IOException;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class AndroidPushFileServiceListener extends DefaultWarpServiceListener {
    private File downloadsDirectory=null;

    @Override
    public void putDefaultValues(Object[] values)
    {
        String publicDirectory;
        if(values != null && values.length == 1)
        {
            publicDirectory = (String)values[0];
        }
        else
        {
            publicDirectory = Environment.DIRECTORY_DOWNLOADS;
        }
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED))
        {
            downloadsDirectory = Environment.getExternalStoragePublicDirectory(publicDirectory);
        }
    }

    @Override
    public void onServiceCompleted(IWarpService servant)
    {
        Object [] result = servant.getResult();
        if(result != null)
        {
            if(result[0] instanceof File)
            {
                File toStore = (File)result[0];
                if(downloadsDirectory != null)
                {
                    try{
                        boolean success = toStore.renameTo(new File(downloadsDirectory,toStore.getName()));
                        if(success && !toStore.exists())
                        {
                            success=toStore.createNewFile();
                            if(success)
                            {
                                //TODO: show notification!
                            }
                        }
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void onServiceProgressUpdate(IWarpService servant)
    {
        //To handle
    }

    @Override
    public void onServiceAbort(String message)
    {
        //TODO: implement
    }
}
