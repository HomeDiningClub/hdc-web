package models.event;

import customUtils.Helpers;
import models.UserProfile;
import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

import java.time.LocalDateTime;

@RelationshipEntity(type = "BOOKED_EVENT_DATE")
public class BookedEventDate extends AbstractEntity {

    @Fetch @StartNode   public UserProfile userProfile;
    @Fetch @EndNode     public EventDate     eventDate;

    // Actual values
    private LocalDateTime bookingDateTime;
    private Boolean approvedByHost;
    private Integer nrOfGuests;
    private String comment;

    // Tracking for when event was changed
    private LocalDateTime bookedAtDateTime;
    private LocalDateTime approvalAtDateTime;


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
        this.approvalAtDateTime = Helpers.getCurrentLocalDateTime();
    }

    public void setNotApprovedByHost(){
        this.approvedByHost = false;
        this.approvalAtDateTime = Helpers.getCurrentLocalDateTime();
    }

    public String getComment(){
        return this.comment;
    }

    public void setComment(String comment){
        this.comment = comment;
    }

    public LocalDateTime getBookingDateTime(){
        return this.bookingDateTime;
    }

    public void setBookingDateTime(EventDate eventDateToBook){
        this.bookingDateTime = eventDateToBook.getEventDateTime();
        this.bookedAtDateTime = Helpers.getCurrentLocalDateTime();
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
}
