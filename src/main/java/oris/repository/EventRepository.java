package oris.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import oris.model.db.Event;

@Repository
public interface EventRepository extends CrudRepository<Event, Long> {

}