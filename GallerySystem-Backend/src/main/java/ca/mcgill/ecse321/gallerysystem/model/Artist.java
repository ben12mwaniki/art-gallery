package ca.mcgill.ecse321.gallerysystem.model;

import javax.persistence.Entity;

import javax.persistence.FetchType;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.OneToMany;

@Entity
public class Artist extends User {

	private String userRole = "artist";
	private Set<ArtPiece> artPieces;

	public void setUserRole(String value) {
		this.userRole = value;
	}

	public String getUserRole() {
		return this.userRole;
	}

	@OneToMany(mappedBy = "artist", cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
	public Set<ArtPiece> getArtPieces() {
		return this.artPieces;
	}

	public void setArtPieces(Set<ArtPiece> artPieces) {
		this.artPieces = artPieces;
	}

}
