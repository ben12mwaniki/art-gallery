// CustomerUpdateRequestDto.java - All fields optional
package ca.mcgill.ecse321.gallerysystem.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

public class CustomerUpdateRequestDto {

    @Size(min = 3, message = "Username must be at least 3 characters")
    private String userName;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private String address;

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    // Helper method to check if a field is present
    public boolean hasUserName() {
        return userName != null;
    }

    public boolean hasPassword() {
        return password != null;
    }

    public boolean hasAddress() {
        return address != null;
    }
}