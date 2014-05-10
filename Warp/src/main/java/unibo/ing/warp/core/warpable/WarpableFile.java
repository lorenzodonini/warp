package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;
import unibo.ing.warp.core.service.base.PushFileService;
import java.io.*;

/**
 * Created by Lorenzo Donini on 5/7/2014.
 */
public class WarpableFile implements IWarpable {
    private File mFile;
    private static final int DEFAULT_BUFFER_SIZE = 10000;
    private int mBufferSize;
    private PushFileService.FileWarpProgressUpdater mUpdater;

    public WarpableFile(PushFileService.FileWarpProgressUpdater updater)
    {
        mBufferSize=DEFAULT_BUFFER_SIZE;
        mUpdater=updater;
    }

    public WarpableFile(int bufferSize,PushFileService.FileWarpProgressUpdater updater)
    {
        mBufferSize = (bufferSize > 0) ? bufferSize : DEFAULT_BUFFER_SIZE;
        mUpdater=updater;
    }

    @Override
    public int warpTo(DataOutputStream outputStream) throws IOException, JSONException
    {
        if(mFile == null)
        {
            return -1;
        }
        else
        {
            byte buffer [] = new byte[mBufferSize];
            int result=0, readBytes, currentStep;
            long totalSize = mFile.length();
            int decimalChunk = (int)totalSize/10;
            FileInputStream fileInputStream = new FileInputStream(mFile);
            while((readBytes = fileInputStream.read(buffer))>= 0)
            {
                result+=readBytes;
                outputStream.write(buffer);
                currentStep = readBytes/decimalChunk;
                mUpdater.notifyProgress(currentStep*10,result);
            }
            fileInputStream.close();
            return result;
        }
    }

    @Override
    public int warpFrom(DataInputStream inputStream) throws IOException, JSONException
    {
        if(mFile != null)
        {
            byte buffer [] = new byte[mBufferSize];
            int result=0, readBytes;
            FileOutputStream fileOutputStream = new FileOutputStream(mFile);
            while ((readBytes=inputStream.read())>=0)
            {
                result +=readBytes;
                fileOutputStream.write(buffer);
            }
            fileOutputStream.close();
            return result;
        }
        return -1;
    }

    @Override
    public Object getValue() throws JSONException
    {
        return null;
    }

    @Override
    public void setValue(Object value)
    {
        mFile=(File)value;
    }

    @Override
    public JSONObject getJSONObject()
    {
        return null;
    }

    @Override
    public void setJSONObject(JSONObject value)
    {
        //Do nothing
    }
}
