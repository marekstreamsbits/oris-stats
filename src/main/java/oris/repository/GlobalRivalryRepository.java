package oris.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oris.model.db.GlobalRivalry;

@Repository
public interface GlobalRivalryRepository extends CrudRepository<GlobalRivalry, Long> {


}