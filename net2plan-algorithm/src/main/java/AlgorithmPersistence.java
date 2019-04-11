import com.shc.easyjson.JSON;
import com.shc.easyjson.JSONArray;
import com.shc.easyjson.JSONObject;
import com.shc.easyjson.JSONValue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AlgorithmPersistence {

    private Map<String,Object> persistenceData = new HashMap();
    private JSONObject persistenceDataJSON = new JSONObject();

    public void addPersistenceData(String key, Object value)
    {

        this.persistenceData.put(key,value);

    }
    public void clear()
    {

        this.persistenceData.clear();

    }
    public String getPersistenceData()
    {

        persistenceDataJSON = new JSONObject();

        for (Map.Entry<String, Object> entry : persistenceData.entrySet()) {

            String newKey = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof List){
                JSONValue jsonValue = addListObject(value);
                if(jsonValue!=null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }else if (value instanceof Map){
                JSONValue jsonValue = addMapObject(value);
                if(value!=null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }else {
                JSONValue jsonValue= addSimpleObject(value);
                if(jsonValue != null)
                    persistenceDataJSON.put(newKey,jsonValue);
            }

        }
        return JSON.write(persistenceDataJSON);

    }
    private JSONValue addSimpleObject(Object value)
    {

        JSONValue jsonValue = null;

        if (value instanceof String) {
            jsonValue = new JSONValue((String) value);
        } else if (value instanceof Integer) {
            jsonValue = new JSONValue((Integer) value);
        } else if (value instanceof Double) {
            jsonValue = new JSONValue((Double) value);
        } else if (value instanceof Long) {
            jsonValue = new JSONValue((Long) value);
        }
        return jsonValue;
    }
    private JSONValue addListObject(Object value)
    {

        JSONArray jsonArray = new JSONArray();

        if(value instanceof List){

            List<Object> list = (List<Object>)value;

            if (list == null || list.size() == 0)
                return new JSONValue(jsonArray);

            if(list.get(0) instanceof List ){
                list.stream().forEach(n-> jsonArray.add(addListObject(n)));

            }else if( list.get(0) instanceof Map){
                list.stream().forEach(n->{ Map<String,Object> map = (Map<String,Object>)n; jsonArray.add(addMapObject(map));});

            } else{
                list.stream().forEach(n-> jsonArray.add(addSimpleObject(n)));

            }

        }

        return new JSONValue(jsonArray);

    }
    private JSONValue addMapObject(Object value)
    {
        Map<String,Object> map = (Map<String,Object>) value;

        JSONObject jsonObject = new JSONObject();

        for (Map.Entry<String, Object> entry : map.entrySet()) {

            String newKey = entry.getKey();
            Object newValue = entry.getValue();

            if (newValue instanceof List){
                JSONValue newJsonA = addListObject(newValue);
                if(newJsonA!=null)
                    jsonObject.put(newKey,newJsonA);

            }else if (newValue instanceof Map){
                JSONValue newJsonA = addMapObject(newValue);
                if(newJsonA!=null)
                    jsonObject.put(newKey,newJsonA);

            }else {
                JSONValue jsonValue= addSimpleObject(newValue);
                if(jsonValue != null)
                    jsonObject.put(newKey,jsonValue);
            }


        }
        return new JSONValue(jsonObject);
    }

}
