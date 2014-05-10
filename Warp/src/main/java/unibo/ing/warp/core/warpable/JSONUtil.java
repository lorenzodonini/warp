package unibo.ing.warp.core.warpable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * User: lorenzodonini
 * Date: 19/11/13
 * Time: 19:24
 */
public class JSONUtil {
    private static final String CLASS_KEY="class";

    /*
    -------------------- PUBLIC REGION --------------------
     */
    /**
     * Uses reflection on the passed Object in order to return a JSONObject,
     * containing all the contents of the parameter itself. The method is generic and
     * can, recursively, put custom Objects inside the result as well.
     * Even though JSONObjects support only String, Integer, Double, Long and Boolean,
     * this method can handle every primitive type, as well as the relative boxed
     * objects. Any POJO and Interface implementation is supported.
     *
     * Since it uses reflection to analyze the fields of the passed object, it is
     * not recommended to use the method non-stop, due to the possibility of serious
     * overhead.
     *
     * Any Object can be put inside a JSONObject, but if the object doesn't have
     * a public empty Constructor, then the jsonToGenericObject(...) method
     * won't be able to recreate the starting object from a Json String. Instead,
     * an InstantiationException will be thrown.
     *
     * @param object  The Object to encapsulate
     * @return  The JSONObject encapsulating the passed parameter and its contents
     */
    public static JSONObject genericObjectToJson(Object object)
    {
        JSONObject result = new JSONObject();
        Class<?> fieldClass;

        putObjectInstantiatedClass(result,object.getClass());
        Field fields [] = object.getClass().getDeclaredFields();
        for(Field f: fields)
        {
            fieldClass=f.getType();
            if(fieldClass.isArray())
            {
                fieldClass=fieldClass.getComponentType();
                putArrayFieldValue(result,f,object, fieldClass);
            }
            else
            {
                putObjectFieldValue(result,f,object,fieldClass);
            }
        }
        return result;
    }

    /**
     * Given a JSONObject, returns the encapsulated Object by creating a new instance
     * of the class passed as a parameter. The method seeks through the declared
     * fields of the given object class, and assigns values, based on the data
     * retrieved inside the JSONObject.
     *
     * The class to be instantiated needs to have a default empty Constructor, e.g.
     * public MyObject() {}
     * If such Constructor is missing, the method will throw an InstantiationException.
     *
     * It is recommended not to overuse this method, because of the massive use of
     * reflection and resource usage in order to dynamically create any kind of
     * Object from a simple JSONObject.
     *
     * @param json  The JSONObject that needs to be parsed
     * @param objectClass  The class type of the result Object. Can be an interface
     *                     or superclass of the actual class
     * @return  The instantiated Object, belonging to the class passed as a parameter
     */
    public static Object jsonToGenericObject(JSONObject json, Class<?> objectClass)
    {
        Class<?> fieldClass;
        Object result;
        Object fieldValue;

        if(json==null)
        {
            return null;
        }

        objectClass=getObjectClassFromJson(json,objectClass);
        result= instantiateObjectByClass(objectClass);
        if(result==null) //Object was still not instantiated
        {
            return null;
        }

        try{
            Field fields [] = objectClass.getDeclaredFields();
            for(Field f: fields)
            {
                fieldClass=f.getType();
                f.setAccessible(true);
                if(fieldClass.isArray())
                {
                    fieldValue=getArrayFieldValue(json,f,fieldClass);
                    if(!fieldClass.isPrimitive())
                    {
                        fieldValue=fieldClass.cast(fieldValue);
                    }
                    f.set(result,fieldValue);
                }
                else
                {
                    fieldValue=getObjectFieldValue(json, f, fieldClass);
                    if(!fieldClass.isPrimitive())
                    {
                        fieldValue=fieldClass.cast(fieldValue);
                    }
                    f.set(result,fieldValue);
                }
            }
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /*
    -------------------- PRIVATE REGION --------------------
     */

    /**
     * This private method is called for each Field contained in an Object by the
     * public genericObjectToJson(...) static method. Given the fieldClass and the
     * fieldValue, passed as an Object, the value is stored inside the JSONObject
     * as a <key,value> pair.
     * The method is not invoked for arrays, but putArrayFieldValue(...) is used instead.
     *
     * The method can process any kind of object, as well as primitive types. For complex
     * custom objects, a new JSONObject will be created and genericObjectToJson(...) will be
     * called recursively, thus placing the new JSONObject inside the original result.
     *
     * Note that the method can process private fields as well, by setting them accessible.
     *
     * @param json  The JSONObject where the object values will be put
     * @param field  The single Field metadata found inside the Object through reflection
     * @param obj  The Object containing the values to be placed inside the JSONObject
     * @param fieldClass  The class of the Field, which can be any kind of class or interface
     */
    private static void putObjectFieldValue(JSONObject json, Field field, Object obj, Class<?> fieldClass)
    {
        try {
            field.setAccessible(true);

            Object fieldValue = field.get(obj);
            /*Checking whether the field value is null. In case it is, the field
            is not going to be added to the JSONObject, and the method returns. */
            if(fieldValue==null)
            {
                return;
            }
            if(fieldClass==Integer.TYPE || fieldClass==Integer.class || fieldClass==String.class
                    || fieldClass==Double.class|| fieldClass==Double.TYPE
                    || fieldClass==Boolean.class || fieldClass==Boolean.TYPE
                    || fieldClass==Long.TYPE || fieldClass==Long.class)
            {
                json.put(field.getName(),fieldValue);
            }
            else if(fieldClass==Byte.TYPE || fieldClass==Byte.class
                    || fieldClass==Character.TYPE || fieldClass==Character.class)
            {
                json.put(field.getName(),fieldValue.toString());
            }
            else if(fieldClass==Float.TYPE || fieldClass==Float.class)
            {
                json.put(field.getName(),((Float)fieldValue).doubleValue());
            }
            else if(fieldClass==Short.TYPE || fieldClass==Short.class)
            {
                json.put(field.getName(),((Short)fieldValue).intValue());
            }
            else
            {
                json.put(field.getName(),genericObjectToJson(fieldValue));
            }
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * This private method is called for Fields contained in an Object that are
     * of Array type. Similarly to the putObjectFieldValue(...) this method is able to process
     * any kind of array, be it primitive types or interfaces. In case the array
     * component type is a complex object, genericoObjecToJson(...) is called recursively
     * for each array element.
     *
     * Note that the method can process private fields as well, by setting them accessible.
     *
     * @param json  The JSONObject where the array values will be put
     * @param field  The single Field metadata found inside the Object through reflection
     * @param obj  The Object containing the values to be placed inside the JSONObject
     * @param fieldClass  The class fo the Field, which can be any kind of class or interface
     */
    private static void putArrayFieldValue(JSONObject json, Field field, Object obj, Class<?> fieldClass)
    {
        JSONArray array = new JSONArray();
        try {
            field.setAccessible(true);

            Object fieldValue = field.get(obj);
            /* Checking whether the array field is initialized or not. In case the field
            is null, no JSONArray object should be added to the passed JSONObject, and the method
            returns. This is not the case if the array is initialized but empty, since in that
            case the array would still be passed, but with no values inside it. */
            if(fieldValue==null)
            {
                return;
            }
            Object arrayMembers [] = (Object [])fieldValue;
            if(fieldClass==Integer.TYPE || fieldClass==Integer.class || fieldClass==String.class
                    || fieldClass==Double.class|| fieldClass==Double.TYPE
                    || fieldClass==Boolean.class || fieldClass==Boolean.TYPE
                    || fieldClass==Long.TYPE || fieldClass==Long.class)
            {
                for (Object arrayMember : arrayMembers)
                {
                    array.put(arrayMember);
                }
            }
            else if(fieldClass==Byte.TYPE || fieldClass==Byte.class
                    || fieldClass==Character.TYPE || fieldClass==Character.class)

            {
                for (Object arrayMember : arrayMembers)
                {
                    array.put(arrayMember.toString());
                }
            }
            else if(fieldClass==Float.TYPE || fieldClass==Float.class)
            {
                for (Object arrayMember : arrayMembers)
                {
                    array.put(((Float) arrayMember).doubleValue());
                }
            }
            else if(fieldClass==Short.TYPE || fieldClass==Short.class)
            {
                for (Object arrayMember : arrayMembers)
                {
                    array.put(((Short) arrayMember).intValue());
                }
            }
            else
            {
                for (Object arrayMember : arrayMembers)
                {
                    array.put(genericObjectToJson(arrayMember));
                }
            }
            json.put(field.getName(),array);
        }
        catch(IllegalAccessException e)
        {
            e.printStackTrace();
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Utility method, needed for custom objects and non-primitive classes. Called by
     * genericObjectToJson(...) it puts the Type, as a class name, inside the JSONObject.
     * This <key,value> pair can be retrieved while recreating the Object from the
     * json string.
     *
     * The class name value is local, meaning it contains the full name of the package.
     *
     * @param json  The JSONObject where the class name will be put
     * @param objectClass  The class of the Object that needs to be incapsulated
     */
    private static void putObjectInstantiatedClass(JSONObject json, Class<?> objectClass)
    {
        if(json != null)
        {
            try {
                json.put(CLASS_KEY,objectClass.getName());
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    }


    private static Object getObjectFieldValue(JSONObject json, Field field, Class<?> fieldClass)
    {
        Object result=null;
        try{
            if(fieldClass==String.class || fieldClass==Integer.TYPE || fieldClass==Integer.class
                    || fieldClass==Double.TYPE || fieldClass==Double.class
                    || fieldClass==Boolean.TYPE || fieldClass==Boolean.class)
            {
                result=json.get(field.getName());
            }
            else if(fieldClass==Long.TYPE || fieldClass==Long.class)
            {
                result=new Double(json.getDouble(field.getName())).longValue();
            }
            else if(fieldClass==Character.TYPE || fieldClass==Character.class)
            {
                result=(json.getString(field.getName())).charAt(0);
            }
            else if(fieldClass==Byte.TYPE || fieldClass==Byte.class)
            {
                result=Byte.decode(json.getString(field.getName()));
            }
            else if(fieldClass==Float.TYPE || fieldClass==Float.class)
            {
                result=new Double(json.getDouble(field.getName())).floatValue();
            }
            else if(fieldClass==Short.TYPE || fieldClass==Short.class)
            {
                result=new Integer(json.getInt(field.getName())).shortValue();
            }
            else
            {
                JSONObject innerObject = json.getJSONObject(field.getName());
                result=jsonToGenericObject(innerObject,fieldClass);
            }
        }
        catch (JSONException e)
        {
            //The Object matching the field was not found, meaning it has to be set null
        }
        return result;
    }

    private static Object [] getArrayFieldValue(JSONObject json, Field field, Class<?> fieldClass)
    {
        Object result [] = null;
        int i;

        try {
            JSONArray array = json.getJSONArray(field.getName());
            Class<?> componentType = fieldClass.getComponentType();
            result= (Object[]) Array.newInstance(componentType, array.length());
            if(componentType==String.class || componentType==Integer.TYPE || componentType==Integer.class
                    || componentType==Double.TYPE || componentType==Double.class
                    || componentType==Boolean.TYPE || componentType==Boolean.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = array.get(i);
                }
            }
            else if(componentType==Long.TYPE || componentType==Long.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = ((Double) array.get(i)).longValue();
                }
            }
            else if(componentType==Character.TYPE || componentType==Character.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = ((String) array.get(i)).charAt(0);
                }
            }
            else if(componentType==Byte.TYPE || componentType==Byte.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = Byte.decode((String) array.get(i));
                }
            }
            else if(componentType==Float.TYPE || componentType==Float.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = ((Double) array.get(i)).floatValue();
                }
            }
            else if(componentType==Short.TYPE || componentType==Short.class)
            {
                for(i=0; i<result.length; i++)
                {
                    result[i] = ((Integer) array.get(i)).shortValue();
                }
            }
            else
            {
                for(i=0; i<result.length; i++)
                {
                    JSONObject innerObject=array.getJSONObject(i);
                    result[i]=jsonToGenericObject(innerObject,componentType);
                }
            }
        }
        catch (JSONException e)
        {
            //The Array was not found, meaning it has to be set null
        }

        return result;
    }

    private static Class<?> getObjectClassFromJson(JSONObject json, Class<?> baseClass)
    {
        String jsonObjectClass;
        Class objectClass=baseClass;

        try{
            jsonObjectClass=json.getString(CLASS_KEY);
            if(jsonObjectClass!=null)
            {
                //CUSTOM BEHAVIOUR
                objectClass=Class.forName(jsonObjectClass);
                objectClass=(objectClass!=baseClass) ? objectClass : baseClass;
            }
        }
        catch (JSONException e)
        {
            /*The JSONObject wasn't well formed and didn't contain any class value.
            Proceeding using the default behaviour, meaning the class will be instantiated
            according to the objectClass parameter passed in input.
            In case the Object to return was an Interface or a superclass of the actual
            objectClass, then the method would throw an InstantiationException. */
        }
        catch (ClassNotFoundException e)
        {
            //The local class does not exist. Let's proceed to default behaviour
        }
        return objectClass;
    }

    private static Object instantiateObjectByClass(Class<?> objectClass)
    {
        Object result = null;

        try{
            //DEFAULT BEHAVIOUR
            result=objectClass.newInstance();
        }
        catch (InstantiationException e)
        {
            e.printStackTrace();
        }
        catch (IllegalAccessException e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
