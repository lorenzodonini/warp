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
    private final static String STRING_KEY="mString";

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
        return (getJSONObject()!=null) ? getJSONObject().getString(STRING_KEY) : null;
    }

    @Override
    public void setValue(Object value)
    {
        mString=(String)value;
    }

    @Override
    public Object getValue() throws JSONException
    {
        return (mString!= null) ? mString : super.getValue();
    }
}
