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

    // Tracking for when event was changed
    private LocalDateTime bookedAtDateTime;
    private LocalDateTime approvalAtDateTime;

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

    public LocalDateTime getBookingDateTime(){
        return this.bookingDateTime;
    }

    public LocalDateTime setBookingDateTime(LocalDateTime date){
        this.bookingDateTime = date;
        this.bookedAtDateTime = Helpers.getCurrentLocalDateTime();
        return this.bookingDateTime;
    }

    public BookedEventDate(UserProfile userProfile, EventDate eventDate, LocalDateTime date) {
        this.userProfile = userProfile;
        this.eventDate = eventDate;
        this.bookingDateTime = date;
        this.approvedByHost = false;
    }

    private BookedEventDate() {

    }
}
