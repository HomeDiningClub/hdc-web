package models.rating;

import models.UserCredential;
import models.base.AuditEntity;
import org.springframework.data.neo4j.annotation.*;

@RelationshipEntity
public class RatingUserCredential extends AuditEntity {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public UserCredential userRates;

    public int ratingValue;
    public String userRaterIP;
    public String ratingComment;

    public RatingUserCredential rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
