package oris.model.db;

import lombok.Data;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

@Data
@Table(name = "event")
@Entity
public class Event extends EventLite {

    private String name;

    @Temporal(TemporalType.DATE)
    private Date date;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
    private Collection<EventStatistics> eventStatistics;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "event")
    private List<Result> results;

    public enum EventType {
        OB(1),
        LOB(2),
        MTBO(3);

        private final int code;

        EventType(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }
}