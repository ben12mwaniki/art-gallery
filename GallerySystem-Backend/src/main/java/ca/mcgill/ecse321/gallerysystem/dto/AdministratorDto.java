package ca.mcgill.ecse321.gallerysystem.dto;

public class AdministratorDto {

	private String userName;
	private String email;

	public AdministratorDto() {
	}

	public AdministratorDto(String userName, String email) {
		this.userName = userName;
		this.email = email;
	}

	// Getters and Setters
	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	// No password getter!
}