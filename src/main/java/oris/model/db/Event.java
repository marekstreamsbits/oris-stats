package oris.model.db;

import javax.persistence.*;
import java.util.Collection;
import java.util.Date;
import java.util.List;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public List<Result> getResults() {
        return results;
    }

    public Collection<EventStatistics> getEventStatistics() {
        return eventStatistics;
    }

    public void setEventStatistics(Collection<EventStatistics> eventStatistics) {
        this.eventStatistics = eventStatistics;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

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