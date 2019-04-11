import com.net2plan.interfaces.networkDesign.IAlgorithm;
import com.net2plan.interfaces.networkDesign.NetPlan;
import com.net2plan.utils.InputParameter;
import com.net2plan.utils.Triple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExampleUsePersistence2 implements IAlgorithm
{

    @Override
    public String executeAlgorithm(NetPlan netPlan, Map<String, String> algorithmParameters, Map<String, String> net2planParameters)
    {

        String resp = executeExample();

        return resp;
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public List<Triple<String, String, String>> getParameters()
    {
        return InputParameter.getInformationAllInputParameterFieldsOfObject(this);
    }

    public String executeExample()
    {
        AlgorithmPersistence algorithmPersistence = new AlgorithmPersistence();

        /*MAKE SOMETHING*/

        /*Example to add information to save*/
        /*Add simple objects*/
        algorithmPersistence.addPersistenceData("latencia",22.1);
        algorithmPersistence.addPersistenceData("origen",2);
        algorithmPersistence.addPersistenceData("destino",3);

        /*Add Lists<Object> or Maps<String,Object> objects*/
        /*Add List of simple object*/
        List<Integer> numbers = new ArrayList();
        numbers.add(3);
        numbers.add(4);
        numbers.add(5);
        algorithmPersistence.addPersistenceData("links_ids",numbers);

        /*Add Map of simple object*/
        Map<String,Object> map = new HashMap<>();
        map.put("id",1);
        map.put("cost",200);
        map.put("distance",3005);
        algorithmPersistence.addPersistenceData("path",map);

        /*Add List of List/Maps object*/
        Map<String,Object> map_level_1_a = new HashMap<>();
        Map<String,Object> map_level_1_b = new HashMap<>();
        Map<String,Object> map_level_2_a = new HashMap<>();
        Map<String,Object> map_level_2_b = new HashMap<>();

        map_level_1_a.put("id",3);
        map_level_1_a.put("capacity",30.0);
        map_level_2_a.put("n_origin",1);
        map_level_2_a.put("n_destin",2);
        map_level_1_a.put("nodes",map_level_2_a);

        map_level_1_b.put("id",4);
        map_level_1_b.put("capacity",25);
        map_level_2_b.put("n_origin",6);
        map_level_2_b.put("n_destin",5);
        map_level_1_b.put("nodes",map_level_2_b);

        List<Map<String,Object>> maps = new ArrayList<>();
        maps.add(map_level_1_a);
        maps.add(map_level_1_b);

        algorithmPersistence.addPersistenceData("links_features",maps);

        /*Get string of JSONObject of persistence information*/
        String resultsToSave = algorithmPersistence.getPersistenceData();

        return resultsToSave;
    }


}
