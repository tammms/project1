package vttp.ssf.project.controller;

import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import vttp.ssf.project.model.Amenities;
import vttp.ssf.project.model.Compare;
import vttp.ssf.project.model.Distance;
import vttp.ssf.project.model.Location;
import vttp.ssf.project.model.User;
import vttp.ssf.project.repository.MapRepo;
import vttp.ssf.project.service.MapService;

@Controller
@RequestMapping(path={"/","home.html"})
public class MapController {

    @Autowired
    MapService mapSvc;

    @Autowired
    MapRepo mapRepo;

    @GetMapping
    public String userForm(Model model){

        User user = new User();
        model.addAttribute("user", user);

        return "home";
    }


    @PostMapping(path="/addUser")
    public String addUser(@Valid @ModelAttribute User user,
                            BindingResult result, Model model, HttpSession sess){
        if (result.hasErrors()){
            return "home";
        }

        String username = user.getName();
        HashMap<String, List<Distance>> mapResults = mapSvc.getDistanceDB(username);


        System.out.println("\naddUser Controller\n");
        System.out.println("\nKeys from cache\n" + mapResults.keySet());

        sess.setAttribute("username", username);
        sess.setAttribute("mapResults", mapResults);

        model.addAttribute("amenities", mapSvc.getAmenitiesList());
        model.addAttribute("location", new Location());
        model.addAttribute("distance", new Distance());
        model.addAttribute("compare", new Compare());
        model.addAttribute("user", user);
        model.addAttribute("username", username);
        model.addAttribute("mapResults", mapResults);

        return "success";
    }

    // remember to validate input postal code - if return no search result, return error

    @GetMapping(path="/input")
    public String showSearch(Model model, HttpSession sess){

    
        String username = (String)sess.getAttribute("username");
        //HashMap<String, List<Distance>> mapResults = (HashMap<String, List<Distance>>)sess.getAttribute("mapResults");
        HashMap<String, List<Distance>> mapResults = mapSvc.getSessDistance(sess);
        
        System.out.println("\nshowSearch Controller\n");
        System.out.println("\nKeys from cache\n" + mapResults.keySet());
        

        model.addAttribute("amenities", mapSvc.getAmenitiesList());
        model.addAttribute("location", new Location());
        model.addAttribute("distance", new Distance());
        model.addAttribute("compare", new Compare());
        model.addAttribute("username", username);
        model.addAttribute("mapResults", mapResults);

        return "success";

    }


    @PostMapping(path="/search")
    public String userSearch(@Valid @ModelAttribute Location location,
                            BindingResult result,
                            @ModelAttribute Compare compare,
                            Model model,
                            @RequestParam MultiValueMap<String,String> params,
                            HttpSession sess){
            
            
            String username = params.getFirst("username");
            String amenitiesAPIValue = params.getFirst("amenities");
            Amenities amenities = mapSvc.getAmenitiesName(amenitiesAPIValue);
            int displayNo = Integer.parseInt(params.getFirst("display"));
            
            model.addAttribute("username", username);
            model.addAttribute("amenities", mapSvc.getAmenitiesList());

            if (result.hasErrors()) {
                
                return "success";}

            location = mapSvc.searchPostalCode(location.getPostalCode());
            
            if (location.getAddress()==null) {
                FieldError err = new FieldError("location", "postalCode",
                                                 "Invalid Postal Code - No results Found");
                result.addError(err);

                return "success";                                 
            }

           HashMap<String, List<Distance>> mapResults = mapSvc.getSessDistance(sess);
           String key = mapRepo.mapKey(username,location.getPostalCode(),amenitiesAPIValue);
           List<Distance> distanceList = new LinkedList<>();

           if(mapResults.containsKey(key)){
            distanceList = mapResults.get(key);
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nResults from cache\n");
           } else{
            distanceList = mapSvc.saveDistances(username, location.getPostalCode(), amenitiesAPIValue);
            mapResults.put(key, distanceList);
            sess.setAttribute("mapResults", mapResults);
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nNew data saved into Redis\n");
           }

           System.out.println("\nSize of Distance List\n" + distanceList.size());

           List<Distance> displayList = mapSvc.displayDistance(distanceList, displayNo);

           

           sess.setAttribute("location", location);
           //sess.setAttribute("distanceList", distanceList);
           sess.setAttribute("displayList", displayList);
           sess.setAttribute("amenities", amenities);

            model.addAttribute("location", location);
            model.addAttribute("compare", compare);
            model.addAttribute("amenities", amenities);
            model.addAttribute("mapResults", mapResults);
            model.addAttribute("displayList", displayList);

        return "search";
    }

    @GetMapping(path = "/search/list")
    public String listDisplay(Model model, HttpSession sess){

        Amenities amenities =(Amenities) sess.getAttribute("amenities");
        Location location =(Location) sess.getAttribute("location");
        List<Distance> displayList = (List<Distance>) sess.getAttribute("displayList");


        model.addAttribute("location", location);
        model.addAttribute("amenities", amenities);
        model.addAttribute("displayList", displayList);

        return "search";
    }

    @GetMapping(path="/search/{postalCode}")
    public String mapDisplay(@PathVariable String postalCode, Model model,
                            HttpSession sess){

        List<Distance> distanceList = (List<Distance>) sess.getAttribute("displayList");

        Location loc1 = (Location)sess.getAttribute("location");
        Distance loc2 = mapSvc.findByPostalCode(postalCode, distanceList);
        
        byte[] map = mapSvc.staticMap(loc1, loc2);

        String mapImg = Base64.getEncoder().encodeToString(map);

        model.addAttribute("mapImg", mapImg);
        model.addAttribute("loc1", loc1);
        model.addAttribute("loc2", loc2);


        return "map";

    }

    @PostMapping(path="/compare")
    public String compare(@Valid @ModelAttribute Compare compare,
                            BindingResult result, Model model,
                            @ModelAttribute Location location,
                            @RequestParam MultiValueMap<String,String> params,
                            HttpSession sess){
        
        Location loc1 = mapSvc.searchPostalCode(compare.getPostalCode1());
        Location loc2 = mapSvc.searchPostalCode(compare.getPostalCode2());

        String username = params.getFirst("username");
        String amenitiesAPIValue = params.getFirst("amenities");
        Amenities amenities = mapSvc.getAmenitiesName(amenitiesAPIValue);
        Double distance = Double.parseDouble(params.getFirst("distance"));
            
        model.addAttribute("username", username);
        model.addAttribute("amenities", mapSvc.getAmenitiesList());



        if (result.hasErrors()) {
                
                return "success";}
            
            if (loc1.getAddress()==null) {
                FieldError err = new FieldError("compare", "postalCode1",
                                                 "Invalid Postal Code - No results Found");
                result.addError(err);

                return "success";                                 
            }

            if (loc2.getAddress()==null) {
                FieldError err = new FieldError("compare", "postalCode2",
                                                 "Invalid Postal Code - No results Found");
                result.addError(err);

                return "success";                                 
            }

            HashMap<String, List<Distance>> mapResults = mapSvc.getSessDistance(sess);
            String key1 = mapRepo.mapKey(username,loc1.getPostalCode(),amenitiesAPIValue);
            String key2 = mapRepo.mapKey(username,loc2.getPostalCode(),amenitiesAPIValue);
            List<Distance> distanceList1 = new LinkedList<>();
            List<Distance> distanceList2 = new LinkedList<>();

           if(mapResults.containsKey(key1)){
            distanceList1 = mapResults.get(key1);

            System.out.println("\nLocation 1\n");
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nResults from cache\n");

           } else{
            distanceList1 = mapSvc.saveDistances(username, loc1.getPostalCode(), amenitiesAPIValue);
            mapResults.put(key1, distanceList1);
            sess.setAttribute("mapResults", mapResults);
            System.out.println("\nLocation 1\n");
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nNew data saved into Redis\n");
           }


           if(mapResults.containsKey(key2)){
            distanceList2 = mapResults.get(key2);

            System.out.println("\nLocation 2\n");
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nResults from cache\n");
           } else{
            distanceList2 = mapSvc.saveDistances(username, loc2.getPostalCode(), amenitiesAPIValue);
            mapResults.put(key2, distanceList2);
            sess.setAttribute("mapResults", mapResults);
            System.out.println("\nLocation 2\n");
            System.out.println("\nKeys\n" + mapResults.keySet());
            System.out.println("\nNew data saved into Redis\n");
           }

           distanceList1 = mapSvc.filterDistance(distanceList1, distance);
           distanceList2 = mapSvc.filterDistance(distanceList2, distance);

           sess.setAttribute("amenitiesInput", amenities);


        model.addAttribute("compare", compare);
        model.addAttribute("amenitiesInput", amenities);
        model.addAttribute("mapResults", mapResults);
        model.addAttribute("location", location);
        model.addAttribute("loc1", loc1);
        model.addAttribute("loc2", loc2);
        model.addAttribute("distanceList1", distanceList1);
        model.addAttribute("distanceList2", distanceList2);
        model.addAttribute("distance", distance);

        System.out.println("\nLocation1\n" + loc1);
         System.out.println("\nLocation2\n" + loc2);

        return "compare";
    }


    @GetMapping("/logout")
    public String logout(HttpSession sess){
        sess.invalidate();

        return "redirect:/";
    }



    
}
