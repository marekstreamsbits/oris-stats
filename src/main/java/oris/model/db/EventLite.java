package oris.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.persistence.Entity;
import javax.persistence.Table;

@Table(name = "event")
@Entity
public class EventLite extends BaseEntity {

    private Long eventId;

    private Integer version;

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getEventId() {
        return eventId;
    }

    @JsonProperty("ID")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}