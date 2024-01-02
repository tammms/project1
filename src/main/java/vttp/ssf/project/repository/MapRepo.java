package vttp.ssf.project.repository;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import vttp.ssf.project.model.Distance;

@Repository
public class MapRepo {

    @Autowired @Qualifier("redisTemplate")
    private RedisTemplate<String, String> template;


    public String mapKey(String name, String postalCode, String amenities){
        return "%s,%s,%s".formatted(name,postalCode,amenities);
    }


    public Set<String> getKeys(String name){
        Set<String> keys = template.keys(name+"*");
        return keys;
    }



    public void saveDistances(String name, String postalCode,
                            String amenities, List<Distance> distanceList){
        
        ListOperations<String, String> opsList = template.opsForList();                        
        String key = mapKey(name, postalCode, amenities);

        for(int i=0; i<distanceList.size(); i++){
            Distance loc = distanceList.get(i);

            String dist = loc.getDistance().toString();
            String startAddress = loc.getStartAddress();
            String endAddress = loc.getEndAddress();
            String endName = loc.getEndName();
            String endPostalCode = loc.getEndPostalCode();
            String lat2 = loc.getLatEnd().toString();
            String lng2 = loc.getLngEnd().toString();

            String value = "%s;%s;%s;%s;%s;%s;%s".
                                    formatted(dist, startAddress, endAddress,
                                            endName, endPostalCode, lat2, lng2);
            opsList.rightPush(key, value);
            template.expire(key, 1, TimeUnit.DAYS);
            //opsList.leftPop(key, 30, TimeUnit.SECONDS);
            
        }

        
    }

    public List<Distance> getDistances(String key){
        ListOperations<String, String> opsList = template.opsForList();                        
        
        List<Distance> distanceList = new LinkedList<>();

        for(int i =0; i<opsList.size(key); i++){
        //for(int i =0; i<2; i++){    
            String value = opsList.index(key, i);
            String[] valueArray = value.split(";");
            Distance dist = new Distance(Double.parseDouble(valueArray[0]),
                                        valueArray[1],valueArray[2],
                                        valueArray[3],valueArray[4],
                                        Double.parseDouble(valueArray[5]),
                                        Double.parseDouble(valueArray[6]));
                                        
            distanceList.add(dist);
            
        }

        return distanceList;

    }




    
}
