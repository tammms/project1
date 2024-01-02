package vttp.ssf.project.controller;

import java.io.StringReader;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import vttp.ssf.project.model.Distance;
import vttp.ssf.project.model.Location;
import vttp.ssf.project.service.MapService;

@RestController
@RequestMapping(path="/process", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProcessController {

    @Autowired
    MapService mapSvc;

    @GetMapping("/themes")
    public ResponseEntity<String> getThemes(){

        ResponseEntity<String> result = mapSvc.getAllThemes();

        return result;
    }

    // use pathvariables to show all the eldercare facilities as json is ok

    @GetMapping(path="/searchPostalCode")
    public ResponseEntity<String> postalCode(@RequestParam String postalCode){
        Location loc = mapSvc.searchPostalCode(postalCode);

        return ResponseEntity.ok(loc.toString());

    }


    @GetMapping(path="/searchAmenities")
    public ResponseEntity<String> amenitiesList(@RequestParam String amenities){
        List<Location> locationList = mapSvc.getAmenities(amenities);

        return ResponseEntity.ok(locationList.toString());

    }

    @PostMapping(path="/distance", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> distance(@RequestBody String payload){

    
        JsonReader jsonReader = Json.createReader(new StringReader(payload));
        JsonObject params = jsonReader.readObject();

        String postalCode = params.getString("postalCode");
        String amenities = params.getString("amenities");

        Location loc = mapSvc.searchPostalCode(postalCode);
        List<Location> locationList = mapSvc.getAmenities(amenities);
        List<Distance> distanceList = mapSvc.getDistance(loc, locationList);

        List<String> distanceString = distanceList.stream()
                                                    .map(dist -> dist.toString())
                                                    .toList();
                                                    

        JsonObject resp = Json.createObjectBuilder()
                                .add("searchPostalCode", postalCode)
                                .add("amenities", amenities)
                                .add("distances", Json.createArrayBuilder(distanceString))
                                .build();
                          

        return ResponseEntity.ok(resp.toString());

    }
    


    
}
