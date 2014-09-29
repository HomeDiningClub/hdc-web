package models.rating;

import models.UserCredential;
import models.base.AuditEntity;
import org.springframework.data.neo4j.annotation.*;

@RelationshipEntity
public class RatesUserCredential extends BaseRating {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public UserCredential userRates;

    public RatesUserCredential rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
