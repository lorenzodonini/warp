package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
public class WarpableUDPResponse extends DefaultWarpableObject {
    private String mMessage;
    private int mPort=-1;
    private final static String MESSAGE_KEY="mMessage";
    private final static String PORT_KEY="mPort";

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
        return mPort;
    }

    public String getMessage()
    {
        return mMessage;
    }

    @Override
    public void setValue(Object value)
    {
        if(value instanceof Integer)
        {
            mPort = (Integer) value;
        }
        else if(value instanceof String)
        {
            mMessage = (String)value;
        }
    }
}
