package vttp.ssf.project.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class User {

    @NotEmpty(message = "Name is Mandatory")
    @Size(min=3, max=20, message = "Name must be between 3 to 20 characters")
    private String name;

    @Pattern(regexp = "(8|9)[0-9]{7}", message = "Invalid Phone number")
    private String phoneNo;

    @Email(message = "Invalid Email format")
    private String email;

    public User(String name, String phoneNo, String email) {
        this.name = name;
        this.phoneNo = phoneNo;
        this.email = email;
    }

    public User(){}

    public String getName() {return name;}
    public void setName(String name) {this.name = name;}

    public String getPhoneNo() {return phoneNo;}
    public void setPhoneNo(String phoneNo) {this.phoneNo = phoneNo;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    @Override
    public String toString() {
        return "User [name=" + name + ", phoneNo=" + phoneNo + ", email=" + email + "]";
    }

    
}
