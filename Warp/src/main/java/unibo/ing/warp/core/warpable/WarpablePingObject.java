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
    public final static String SERVICES_KEY = "pingServices";
    public final static String CODE_KEY = "pingCode";
    public final static String MAC_KEY = "pingMac";

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
        return null;
    }

    @Override
    public Object getValue(String key)
    {
        if(key == null)
        {
            return null;
        }
        if(key.equals(SERVICES_KEY))
        {
            return mServices;
        }
        else if(key.equals(MAC_KEY))
        {
            return mMacAddress;
        }
        else if(key.equals(CODE_KEY))
        {
            return mPingCode;
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(value == null || key == null)
        {
            return;
        }
        if(key.equals(SERVICES_KEY))
        {
            mServices = (String [])value;
        }
        else if(key.equals(MAC_KEY))
        {
            mMacAddress = (String) value;
        }
        else if(key.equals(CODE_KEY))
        {
            mPingCode = (Byte) value;
        }
    }
}
