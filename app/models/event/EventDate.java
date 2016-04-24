package models.event;

import models.base.AuditEntity;
import org.springframework.data.neo4j.annotation.NodeEntity;

import java.time.LocalDateTime;

@NodeEntity
public class EventDate extends AuditEntity {

    private LocalDateTime eventDateTime;

    public void setEventDateTime(LocalDateTime date){
        this.eventDateTime = date;
    }
    public LocalDateTime getEventDateTime(){
        return this.eventDateTime;
    }

    public EventDate(LocalDateTime date){
        setEventDateTime(date);
    }
    public EventDate(){
        setEventDateTime(customUtils.Helpers.getCurrentLocalDateTime());
    }
}
