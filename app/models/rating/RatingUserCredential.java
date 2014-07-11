package models.rating;

import models.UserCredential;
import models.base.AbstractEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.annotation.*;

@RelationshipEntity
public class RatingUserCredential extends AbstractEntity {

    @StartNode
    private UserCredential userWhoIsRating;

    @EndNode
    private UserCredential userRates;

    public int ratingValue;
    public String userRaterIP;
    String ratingComment;

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    public RatingUserCredential rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
