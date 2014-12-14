package unibo.ing.warp.core.warpable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 12/11/2014.
 */
public class WarpablePingObject extends DefaultWarpableObject {
    private String mMacAddress;
    private String [] mServices;
    private int mPingCode;
    private final static String SERVICES_KEY = "mServices";
    private final static String CODE_KEY = "mCode";
    private final static String MAC_KEY = "mMac";

    public WarpablePingObject() { super(); }

    public WarpablePingObject(JSONObject json) { super(json); }

    public WarpablePingObject(String mac, byte code, String [] services)
    {
        mMacAddress = mac;
        mServices = services;
        mPingCode = code;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(CODE_KEY, mPingCode);
        json.put(MAC_KEY, mMacAddress);
        if(mServices != null && mServices.length > 0)
        {
            JSONArray arr = new JSONArray();
            for(String s: mServices)
            {
                arr.put(s);
            }
            json.put(SERVICES_KEY,arr);
        }
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
        mPingCode = (Integer)json.get(CODE_KEY);
        mMacAddress = json.getString(MAC_KEY);
        JSONArray arr = json.optJSONArray(SERVICES_KEY);
        if(arr != null)
        {
            mServices = new String[arr.length()];
            for(int i=0; i<arr.length(); i++)
            {
                mServices[i] = arr.getString(i);
            }
        }
        else
        {
            mServices = null;
        }
        return mPingCode;
    }

    public String [] getServices()
    {
        return mServices;
    }

    public String getMacAddress()
    {
        return mMacAddress;
    }

    @Override
    public void setValue(Object value)
    {
        if(value == null)
        {
            return;
        }
        if(value instanceof Byte)
        {
            mPingCode = (Byte) value;
        }
        else if(value instanceof String [])
        {
            mServices = (String []) value;
        }
        else if(value instanceof String)
        {
            mMacAddress = (String) value;
        }
    }
}
