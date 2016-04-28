package models.event;

import models.base.AuditEntity;
import org.springframework.data.neo4j.annotation.NodeEntity;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@NodeEntity
public class EventDate extends AuditEntity {

    private Date eventDateTime;

    public void setEventDateTime(LocalDateTime date){
        this.eventDateTime = castToDate(date);
    }
    public LocalDateTime getEventDateTime(){
        return castToLocalDateTime(this.eventDateTime);
    }

    public EventDate(LocalDateTime date){
        setEventDateTime(date);
    }
    public EventDate(){
        setEventDateTime(customUtils.Helpers.getCurrentLocalDateTime());
    }

    private Date castToDate(LocalDateTime localDateTime){
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime castToLocalDateTime(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
