package oris.model.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Data
@Table(name = "event")
@Entity
public class EventLite extends BaseEntity {

    @Column(nullable = false)
    private Long eventId;

    private Integer version;

    @JsonProperty("ID")
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }
}