package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;
import java.nio.ByteBuffer;

/**
 * Created with IntelliJ IDEA.
 * User: lorenzodonini
 * Date: 12/11/13
 * Time: 15:50
 */
public class WarpableDouble extends DefaultWarpableObject {
    private Double mDouble;
    public final static String DOUBLE_KEY="mDouble";

    public WarpableDouble()
    {
        super();
    }

    public WarpableDouble(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    public WarpableDouble(double d)
    {
        mDouble=d;
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject obj = new JSONObject();
        obj.put(DOUBLE_KEY,mDouble);
        return obj;
    }

    @Override
    protected Object fromJSONObject() throws JSONException
    {
        if(getJSONObject() == null)
        {
            return null;
        }
        mDouble = getJSONObject().getDouble(DOUBLE_KEY);
        return mDouble;
    }

    @Override
    public void setValue(String key, Object value)
    {
        if(value != null && key != null && key.equals(DOUBLE_KEY))
        {
            mDouble = (Double) value;
        }
    }

    @Override
    public Object getValue(String key) throws JSONException
    {
        if(key != null && mDouble != null && key.equals(DOUBLE_KEY))
        {
            return mDouble;
        }
        return super.getValue(key);
    }

    @Override
    public Object getValue() throws JSONException
    {
        return mDouble;
    }
}
