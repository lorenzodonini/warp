package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * User: lorenzodonini
 * Date: 18/11/13
 * Time: 20:14
 */
public abstract class DefaultWarpableObject implements IWarpable {
    private JSONObject mJsonObject;
    protected final static int FORMAT_CHAR_SIZE=1;

    public DefaultWarpableObject()
    {
        mJsonObject=null;
    }

    public DefaultWarpableObject(JSONObject jsonObject)
    {
        mJsonObject=jsonObject;
    }

    @Override
    public int warpTo(DataOutputStream outputStream) throws IOException, JSONException
    {
        if(mJsonObject==null)
        {
            mJsonObject=toJSONObject();
        }
        if(mJsonObject!=null)
        {
            String toSend=mJsonObject.toString();
            outputStream.writeUTF(toSend);

            return toSend.length()*FORMAT_CHAR_SIZE;
        }
        return -1;
    }

    @Override
    public int warpFrom(DataInputStream inputStream) throws IOException, JSONException
    {
        String received=inputStream.readUTF();
        mJsonObject=new JSONObject(received);

        return received.length()*FORMAT_CHAR_SIZE;
    }

    @Override
    public byte[] warpTo() throws JSONException
    {
        if(mJsonObject==null)
        {
            mJsonObject=toJSONObject();
        }
        if(mJsonObject != null)
        {
            String toSend=mJsonObject.toString();
            return toSend.getBytes();
        }
        return null;
    }

    @Override
     public int warpFrom(byte [] buffer) throws JSONException {
        String received = new String(buffer);
        mJsonObject = new JSONObject(received);
        fromJSONObject();

        return received.length()*FORMAT_CHAR_SIZE;
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(mJsonObject!=null)
        {
            return fromJSONObject();
        }
        return null;
    }

    public Object getValue() throws JSONException
    {
        return getValue(null);
    }

    @Override
    public JSONObject getJSONObject()
    {
        return mJsonObject;
    }

    @Override
    public void setJSONObject(JSONObject value)
    {
        mJsonObject=value;
    }

    protected abstract JSONObject toJSONObject() throws JSONException;
    protected abstract Object fromJSONObject() throws JSONException;
}
