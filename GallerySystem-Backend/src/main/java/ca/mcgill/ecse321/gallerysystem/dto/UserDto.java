package ca.mcgill.ecse321.gallerysystem.dto;

public class UserDto {

	private String userRole;
	private String email;
	private String userName;

	public UserDto() {

	}

	public UserDto(String userName, String email, String userRole) {
		this.email = email;
		this.userName = userName;
		this.userRole = userRole;
	}

	public String getEmail() {
		return this.email;
	}

	public String getUserRole() {
		return this.userRole;
	}

	public String getUserName() {
		return this.userName;
	}

}
