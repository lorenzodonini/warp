package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
public class WarpableUDPResponse extends DefaultWarpableObject {
    private String mMessage;
    private Integer mPort=-1;
    public final static String MESSAGE_KEY="mMessage";
    public final static String PORT_KEY="mPort";

    public WarpableUDPResponse()
    {
        super();
    }

    public WarpableUDPResponse(String message, int port)
    {
        mMessage=message;
        mPort=port;
    }

    public WarpableUDPResponse(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(PORT_KEY,mPort);
        json.put(MESSAGE_KEY,mMessage);
        return json;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        JSONObject json = getJSONObject();
        if(json == null)
        {
            return null;
        }
        mMessage = json.getString(MESSAGE_KEY);
        mPort = json.getInt(PORT_KEY);
        return null;
    }

    public String getMessage()
    {
        return mMessage;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(value == null || key == null)
        {
            return;
        }
        if(key.equals(PORT_KEY))
        {
            mPort = (Integer)value;
        }
        else if(key.equals(MESSAGE_KEY))
        {
            mMessage = (String)value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key == null)
        {
            return null;
        }
        if(key.equals(PORT_KEY) && mPort != null)
        {
            return mPort;
        }
        else if(key.equals(MESSAGE_KEY) && mMessage != null)
        {
            return mMessage;
        }
        return super.getValue(key);
    }
}
