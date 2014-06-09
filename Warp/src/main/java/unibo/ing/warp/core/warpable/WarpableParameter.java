package unibo.ing.warp.core.warpable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Lorenzo Donini on 6/4/2014.
 */
public class WarpableParameter extends DefaultWarpableObject {
    private IWarpable mParameter;
    private static final String PARAM_CLASS = "mParamClass";
    private static final String PARAM_VALUE = "mValue";

    public WarpableParameter()
    {
        super();
    }

    public WarpableParameter(IWarpable parameter)
    {
        mParameter=parameter;
    }

    public WarpableParameter(JSONObject jsonObject)
    {
        super(jsonObject);
    }

    @Override
    protected JSONObject toJSONObject() throws JSONException
    {
        JSONObject json = new JSONObject();
        //Passing Warpable subclass, not object class
        json.put(PARAM_CLASS,mParameter.getValue().getClass().getName());
        json.put(PARAM_VALUE,mParameter.getJSONObject());
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
        JSONObject value = json.getJSONObject(PARAM_VALUE);
        try{
            @SuppressWarnings("unchecked")
            Class<? extends IWarpable> warpableClass = (Class<? extends IWarpable>)
                    Class.forName(json.getString(PARAM_CLASS));
            mParameter = warpableClass.newInstance();
            mParameter.setJSONObject(value);
        }
        catch(Exception e)
        {
            //TODO: handle exception
            return null;
        }
        return mParameter;
    }

    @Override
    public void setValue(Object value)
    {
        mParameter = (IWarpable)value;
    }
}
