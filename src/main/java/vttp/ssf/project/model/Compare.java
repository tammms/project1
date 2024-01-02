package vttp.ssf.project.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class Compare {

    @NotEmpty(message = "Postal Code is a Mandatory Field")
    @Pattern(regexp = "[0-9]{6}", message = "Invalid Postal Code")
    private String postalCode1;

    @NotEmpty(message = "Postal Code is a Mandatory Field")
    @Pattern(regexp = "[0-9]{6}", message = "Invalid Postal Code")
    private String postalCode2;
    


    public Compare(String postalCode1, String postalCode2) {
        this.postalCode1 = postalCode1;
        this.postalCode2 = postalCode2;
    }

    public Compare(){}

    public String getPostalCode1() {return postalCode1;}
    public void setPostalCode1(String postalCode1) {this.postalCode1 = postalCode1;}


    public String getPostalCode2() {return postalCode2;}
    public void setPostalCode2(String postalCode2) {this.postalCode2 = postalCode2;}

    
    
}
