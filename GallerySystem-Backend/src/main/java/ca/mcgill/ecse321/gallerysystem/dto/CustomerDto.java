package ca.mcgill.ecse321.gallerysystem.dto;

public class CustomerDto {

	private String address;
	private String email;
	private String userName;

	public CustomerDto() {
	}

	public CustomerDto(String userName, String address, String email) {
		this.userName = userName;
		this.address = address;
		this.email = email;
	}

	public String getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	public Object getUserName() {
		return userName;
	}
}