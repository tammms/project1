package vttp.ssf.project.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.ByteArrayHttpMessageConverter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.servlet.http.HttpSession;
import vttp.ssf.project.model.Amenities;
import vttp.ssf.project.model.Distance;
import vttp.ssf.project.model.Location;
import vttp.ssf.project.repository.MapRepo;

@Service
public class MapService {

    private String apiKey;

    @Value("${api.email}")
    private String email;

	@Value("${api.password}")
    private String password;

    @Autowired
    MapRepo mapRepo;

    private List<Amenities> amenitiesList = null;

    @Autowired
    public MapService(@Value("${api.key}") String apiKey) {
        this.apiKey = apiKey;
    }
    

    public String getToken() throws Exception{

        String apiUrl = "https://www.onemap.gov.sg/api/auth/post/getToken";

        String inputDetails = String.format("{\"email\": \"%s\", \"password\": \"%s\"}", email, password);

        URI uri = new URI(apiUrl);

        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.write(inputDetails.getBytes(StandardCharsets.UTF_8));

        JsonReader reader = Json.createReader(new InputStreamReader(connection.getInputStream()));
        JsonObject object = reader.readObject();
        apiKey = object.get("access_token").toString().replace("\"", "");
            
            // Close the connection
        connection.disconnect();

        return apiKey;
    }



    public String getPayload(String url){

        RequestEntity<Void> req = RequestEntity.get(url)
                                .header("Authorization", "Bearer " + apiKey)
                                .build();
        
        RestTemplate template = new RestTemplate();
        ResponseEntity<String> resp = template.exchange(req, String.class);

        String payload = resp.getBody();
        return payload;
    }


    public Location searchPostalCode(String postalCode){

        String url = UriComponentsBuilder
                .fromUriString("https://www.onemap.gov.sg/api/common/elastic/search")
                .queryParam("searchVal", postalCode)
                .queryParam("returnGeom", "Y")
                .queryParam("getAddrDetails", "N")
                .toUriString();

        String payload = getPayload(url);
        JsonReader jsonReader = Json.createReader(new StringReader(payload));
        JsonObject result = jsonReader.readObject();

        String entriesFound = result.get("found").toString();

        Location loc = new Location();

        if(!entriesFound.equals("0")){
            JsonObject results = result.getJsonArray("results").getJsonObject(0);
            loc.setAddress(results.getString("SEARCHVAL"));
            loc.setLatitude(Double.parseDouble(results.getString("LATITUDE")));
            loc.setLongtitude(Double.parseDouble(results.getString("LONGITUDE")));
            loc.setPostalCode(postalCode);
        }
            
            //System.out.println("\nResult:\n" + result.get("found").toString().equals("1"));
            //System.out.println("\nResult:\n" + results);
            //System.out.println("\nLocation Result:\n" + loc.toString());
            // System.out.println("\nResult get searchval:\n" + results.getString("SEARCHVAL"));
            //System.out.println("\n");

        return loc;

    }

    public ResponseEntity<String> getAllThemes(){

        String url = UriComponentsBuilder
                .fromUriString("https://www.onemap.gov.sg/api/public/themesvc/getAllThemesInfo")
                .toUriString();

        String payload = getPayload(url);
        JsonReader jsonReader = Json.createReader(new StringReader(payload));
        JsonObject results = jsonReader.readObject();

        List<String> themeList = results.getJsonArray("Theme_Names")
                .stream()
                .map(j -> j.asJsonObject())
                .map(o -> {
                    String theme = "\n%s,%s".formatted(o.getString("THEMENAME"),o.getString("QUERYNAME"));
                    return theme;
                })
                .collect(Collectors.toList());
        
        // System.out.println("\nResult:\n" + results.getJsonArray("Theme_Names").size());
        // System.out.println("\nTHEMES:\n" + themeList);

        return ResponseEntity.ok(results.toString());

    }

    public List<Location> getAmenities(String amenities){

        String url = UriComponentsBuilder
                .fromUriString("https://www.onemap.gov.sg/api/public/themesvc/retrieveTheme")
                .queryParam("queryName", amenities)
                .toUriString();

        String payload = getPayload(url);
        JsonReader jsonReader = Json.createReader(new StringReader(payload));
        JsonObject results = jsonReader.readObject();
        List<Location> locations = results.getJsonArray("SrchResults")
                        .stream()
                        .skip(1)
                        .map(j -> j.asJsonObject())
                        .map(o -> {
                            String postalCode = o.getString("ADDRESSPOSTALCODE");
                            String name = o.getString("NAME","na");
                            String address = o.getString("ADDRESSSTREETNAME", "na");
                            String[] latLng = o.getString("LatLng").replaceAll("[^\\s0-9,.]","").split(",");
                            double lat = Double.parseDouble(latLng[0]);
                            double lng = Double.parseDouble(latLng[1]);
                            return new Location(postalCode, name, address, lat, lng);
                        })
                        .toList();

        List<Location> locationList = locations.stream()
                                     .filter(location -> !location.getAddress().equals("na")&&
                                                    !location.getName().equals("na"))  
                                    .toList();              

        //System.out.println("\nResult:\n" + results.getJsonArray("SrchResults").getJsonObject(1));
        //System.out.println("\nResult:\n" + locationList);

        return locationList;

    }

    



    private double deg2rad (double deg) {return (deg * Math.PI / 180.0);}
    private double rad2deg (double rad) {return (rad * 180.0 / Math.PI);}

    public List<Distance> getDistance(Location inputLocation, List<Location> locationList){

        double lat1 = inputLocation.getLatitude();
        double lng1 = inputLocation.getLongtitude();
        String startAddress = inputLocation.getAddress();

        //https://stackoverflow.com/questions/3694380/calculating-distance-between-two-points-using-latitude-longitude

        List<Distance> distanceList = locationList.stream()
                    .map(loc ->{
                        String endAddress = loc.getAddress();
                        String endName = loc.getName();
                        String endPostalCode = loc.getPostalCode();
                        double lat2 = loc.getLatitude();
                        double lng2 = loc.getLongtitude();
                        double theta = lng1 -lng2;
                        Double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
                        dist = Math.acos(dist);
                        dist = rad2deg(dist);
                        dist = dist * 60 * 1.1515 * 1.609344;
                        DecimalFormat df = new DecimalFormat("#.##");
                        dist = Double.valueOf(df.format(dist));
                        return 
                        new Distance(dist, startAddress, endAddress, endName, endPostalCode,
                                      lat2,lng2);
                    })
                    .sorted((d0, d1) -> d0.getDistance().compareTo(d1.getDistance()))
                    .toList();

        //System.out.println("\nResult:\n" + distanceList);
        
        return distanceList;

    }


    public HashMap<String, List<Distance>> getSessDistance(HttpSession sess){
        HashMap<String, List<Distance>> mapResults =
                                 (HashMap<String, List<Distance>>)sess.getAttribute("mapResults");
        
        if(mapResults==null){
            mapResults = new HashMap<>();
            sess.setAttribute("mapResults", mapResults);
        }
        return mapResults;
    }




    public HashMap<String, List<Distance>> getDistanceDB(String name){
        HashMap<String, List<Distance>> mapDB = new HashMap<>();
        Set<String> keys = mapRepo.getKeys(name);

        if(!keys.isEmpty()){
           String[] keyArray = keys.toArray(new String[keys.size()]);
           for(int i = 0; i<keyArray.length; i++){
            List<Distance> dist = mapRepo.getDistances(keyArray[i]);
            mapDB.put(keyArray[i], dist);
           }
        //System.out.println("\nResult:\n" + keyArray[0]);
        }

        return mapDB;
    }


    public List<Distance> saveDistances(String name, String postalCode, String amenities){

        Location loc = searchPostalCode(postalCode);
        List<Location> amenitiesLocations = getAmenities(amenities);
        List<Distance> distancesList = getDistance(loc, amenitiesLocations);                             
        mapRepo.saveDistances(name, postalCode, amenities, distancesList);

        return distancesList;

    }


    public List<Amenities> getAmenitiesList(){

        if(amenitiesList == null){
            amenitiesList = new LinkedList<>();

            for(int i=0; i<Location.amenities_values.length; i++){
                Amenities am = new Amenities(Location.amenities_values[i], Location.amenities_text[i]);
                amenitiesList.add(am);
                //System.out.println("\nResult:\n" + amenitiesList);
            }
        }
        //System.out.println("\nResult:\n" + amenitiesList);
        return amenitiesList;
    }


    public Amenities getAmenitiesName(String inputAmenities){
        
        List<Amenities> amenitiesList = getAmenitiesList();
        return amenitiesList.stream()
                                    .filter(ams -> ams.apiValue().equals(inputAmenities))
                                    .findFirst()
                                    .get();
        
    }

    public List<Distance> displayDistance(List<Distance> distanceList, Integer displayNo){
        
        List<Distance> displayList = distanceList.stream()
                                                .limit(displayNo)
                                                .toList();

       return displayList;                                         
    }

    public List<Distance> filterDistance(List<Distance> distanceList, Double distance){
        
        List<Distance> displayList = distanceList.stream()
                                                .filter(loc -> loc.getDistance()<= distance)
                                                .toList();

       return displayList;                                         
    }

    public Distance findByPostalCode(String postalCode, List<Distance> distanceList){
        return distanceList.stream()
                            .filter(loc -> loc.getEndPostalCode().equals(postalCode))
                            .findFirst()
                            .get();
    }


    public byte[] staticMap(Location loc1, Distance loc2){

        // https://stackoverflow.com/questions/4656802/midpoint-between-two-latitude-and-longitude

        loc1 = searchPostalCode(loc1.getPostalCode());
        
        Double lat1 = loc1.getLatitude();
        Double lng1 = loc1.getLongtitude();

        Double lat2 = loc2.getLatEnd();
        Double lng2 = loc2.getLngEnd();

        //convert to radians
        Double dLng = Math.toRadians(lng2 - lng1);

        //convert to radians
        Double radLat1 = Math.toRadians(lat1);
        Double radLat2 = Math.toRadians(lat2);
        Double radLng1 = Math.toRadians(lng1);

        Double Bx = Math.cos(radLat2) * Math.cos(dLng);
        Double By = Math.cos(radLat2) * Math.sin(dLng);
        Double lat3 = Math.atan2(Math.sin(radLat1) + Math.sin(radLat2), Math.sqrt((Math.cos(radLat1) + Bx) * (Math.cos(radLat1) + Bx) + By * By));
        Double lng3 = radLng1 + Math.atan2(By, Math.cos(radLat1) + Bx);

        lat3= Math.toDegrees(lat3);
        lng3 = Math.toDegrees(lng3);

        
        String points = String.format("[%f,%f]|[%f,%f]", lat1, lng1, lat2, lng2);

        String url = UriComponentsBuilder
                .fromUriString("https://www.onemap.gov.sg/api/staticmap/getStaticImage")
                .queryParam("layerchosen", "default")
                .queryParam("latitude", lat3.toString())
                .queryParam("longitude", lng3.toString())
                .queryParam("zoom", "16")
                .queryParam("width", "512")
                .queryParam("height", "512")
                .toUriString();

        url = String.format("%s&points=%s&color=%s&fillColor=%s", url,points,"255,0,255", "0,255,0");
        

        RequestEntity<Void> req = RequestEntity.get(url)
                                .header("Authorization", "Bearer " + apiKey)
                                .build();
        
        RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(new ByteArrayHttpMessageConverter());
        ResponseEntity<byte[]> resp = template.exchange(req, byte[].class);

        byte[] payload = resp.getBody();

        return payload;


    }

    
}
