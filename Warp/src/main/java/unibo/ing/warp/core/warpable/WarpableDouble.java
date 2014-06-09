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
    private final static String DOUBLE_KEY="mDouble";

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
        return (getJSONObject()!=null) ? getJSONObject().getDouble(DOUBLE_KEY) : null;
    }

    @Override
    public void setValue(Object value)
    {
        mDouble=(Double)value;
    }

    @Override
    public Object getValue() throws JSONException
    {
        return (mDouble!=null) ? mDouble : super.getValue();
    }
}
