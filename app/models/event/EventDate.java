package models.event;

import models.base.AuditEntity;
import java.util.Date;
import utils.Helpers;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class EventDate extends AuditEntity {

    private Date eventDateTime;

    public void setEventDateTime(Date date){
        this.eventDateTime = date;
    }
    public Date getEventDateTime(){
        return this.eventDateTime;
    }

    public EventDate(Date date){
        setEventDateTime(date);
    }
    public EventDate(){
        setEventDateTime(utils.Helpers.getCurrentDateTime());
    }
}
