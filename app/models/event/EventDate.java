package models.event;

import models.Event;
import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.annotation.RelatedToVia;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class EventDate extends AuditEntity {

    private Date eventDateTime;

    @RelatedToVia(type = RelationshipTypesJava.BOOKED_EVENT_DATE.Constant, direction = Direction.INCOMING)
    private Set<BookedEventDate> bookedEventDates;

    @RelatedTo(type = RelationshipTypesJava.EVENT_TIMES.Constant, direction = Direction.INCOMING)
    private Event ownerEvent;

    public Boolean deleteBooking(BookedEventDate bookingToAdd){
        return this.bookedEventDates != null && this.bookedEventDates.removeIf(b -> b.objectId.equals(bookingToAdd.objectId));
    }

    public BookedEventDate addOrUpdateBooking(BookedEventDate bookingToAdd){
        if(this.bookedEventDates == null) {
            this.bookedEventDates = new HashSet<>();
        }else {
            this.bookedEventDates.removeIf(b -> b.objectId.equals(bookingToAdd.objectId));
        }

        this.bookedEventDates.add(bookingToAdd);
        return bookingToAdd;
    }

    public Set<BookedEventDate> getBookings(){
        if(this.bookedEventDates == null)
            this.bookedEventDates = new HashSet<>();

        return this.bookedEventDates;
    }

    public Event getOwnerEvent(){
        return this.ownerEvent;
    }

    public int getGuestsBooked(){
        int nrOfTotalGuests = 0;

        if(!this.getBookings().isEmpty()){
            for (BookedEventDate booking : this.getBookings()) {
                if(booking.getApprovedByHost()) {
                    nrOfTotalGuests += booking.getNrOfGuests();
                }
            }
        }

        return nrOfTotalGuests;
    }


    public int getSpacesLeft(){
        return this.getMaxNrOfGuests() - this.getGuestsBooked();
    }

    public int getMaxNrOfGuests(){
        return getOwnerEvent().getMaxNrOfGuests();
    }

    public void setEventDateTime(LocalDateTime date){
        this.eventDateTime = castToDate(date);
    }

    public LocalDateTime getEventDateTime(){
        return castToLocalDateTime(this.eventDateTime);
    }

    public EventDate(LocalDateTime date){
        setEventDateTime(date);
    }

    private EventDate(){
        //setEventDateTime(customUtils.Helpers.getCurrentLocalDateTime());
    }

    private Date castToDate(LocalDateTime localDateTime){
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime castToLocalDateTime(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
