package ca.mcgill.ecse321.gallerysystem.dao;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import ca.mcgill.ecse321.gallerysystem.model.Administrator;

@RepositoryRestResource(collectionResourceRel = "admin_data", path = "admin_data")
public interface AdministratorRepository extends CrudRepository<Administrator, String>{
    Administrator findAdministratorByEmail(String adminEmail);

}
