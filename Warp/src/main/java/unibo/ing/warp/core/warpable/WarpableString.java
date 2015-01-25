package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * User: lorenzodonini
 * Date: 12/11/13
 * Time: 16:03
 */
public class WarpableString extends DefaultWarpableObject {
    private String mString;
    public final static String STRING_KEY="mString";

    public WarpableString()
    {
        super();
    }

    public WarpableString(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public WarpableString(String s)
    {
        mString=s;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(STRING_KEY,mString);
        return obj;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        if(getJSONObject() == null)
        {
            return null;
        }
        mString = getJSONObject().getString(STRING_KEY);
        return mString;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(key != null && value != null && key.equals(STRING_KEY))
        {
            mString = (String) value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && mString != null && key.equals(STRING_KEY))
        {
            return mString;
        }
        return super.getValue(key);
    }
}
