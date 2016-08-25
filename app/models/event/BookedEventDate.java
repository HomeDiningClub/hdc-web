package models.event;

import customUtils.Helpers;
import models.UserProfile;
import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@RelationshipEntity(type = "BOOKED_EVENT_DATE")
public class BookedEventDate extends AbstractEntity {

    @StartNode   public UserProfile userProfile;
    @Fetch @EndNode     public EventDate     eventDate;

    // Actual values
    private Date bookingDateTime;
    private Boolean approvedByHost;
    private Integer nrOfGuests;
    private String comment;

    // Tracking for when event was changed
    private Date bookedAtDateTime;
    private Date approvalAtDateTime; // Not used yet, but stored


    public Integer getNrOfGuests(){
        return this.nrOfGuests;
    }

    public void setNrOfGuests(Integer nrOfGuestsToBeBooked){
        this.nrOfGuests = nrOfGuestsToBeBooked;
    }

    public Boolean getApprovedByHost(){
        return this.approvedByHost;
    }

    public void setApprovedByHost(){
        this.approvedByHost = true;
        this.approvalAtDateTime = castToDate(Helpers.getCurrentLocalDateTime());
    }

    public void setNotApprovedByHost(){
        this.approvedByHost = false;
        this.approvalAtDateTime = castToDate(Helpers.getCurrentLocalDateTime());
    }

    public String getComment(){
        return this.comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public LocalDateTime getBookingDateTime(){
        return castToLocalDateTime(this.bookingDateTime);
    }

    public LocalDateTime getBookedAtDateTime(){
        return castToLocalDateTime(this.bookedAtDateTime);
    }


    public void setBookingDateTime(EventDate eventDateToBook){
        this.bookingDateTime = castToDate(eventDateToBook.getEventDateTime());
        this.bookedAtDateTime = castToDate(Helpers.getCurrentLocalDateTime());
    }

    public BookedEventDate(UserProfile userBooking, Integer nrOfGuestsToBeBooked, EventDate eventDateToBook, String comment) {
        this.setComment(comment);
        this.userProfile = userBooking;
        this.eventDate = eventDateToBook;
        this.setNrOfGuests(nrOfGuestsToBeBooked);
        this.setBookingDateTime(eventDateToBook);
        this.setApprovedByHost();
    }

    private BookedEventDate() {

    }

    private Date castToDate(LocalDateTime localDateTime){
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime castToLocalDateTime(Date date){
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }
}
