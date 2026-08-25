package ca.mcgill.ecse321.gallerysystem.dao;

import java.util.Set;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.ArtPiece;
import ca.mcgill.ecse321.gallerysystem.model.Artist;

@RepositoryRestResource(collectionResourceRel = "artpiece_data", path = "artpiece_data")
public interface ArtPieceRepository extends CrudRepository<ArtPiece, Integer> {

	ArtPiece findArtPieceByArtID(Integer artID);

	ArtPiece findArtPieceByArtistEmail(String email);

	long deleteArtPieceByArtID(Integer artID);

	long deleteArtPieceByArtistEmail(String email);

	Set<ArtPiece> findByArtist(Artist artist);

}
