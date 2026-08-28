package ca.mcgill.ecse321.gallerysystem.dto;

public class ArtistDto {

	private String email;
	private String userName;

	public ArtistDto() {
	}

	public ArtistDto(String userName, String email) {
		this.userName = userName;
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public String getUserName() {
		return userName;
	}
}