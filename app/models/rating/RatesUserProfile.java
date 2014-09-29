package models.rating;

import models.UserCredential;
import models.UserProfile;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity
public class RatesUserProfile extends BaseRating {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public UserProfile userRates;

    public RatesUserProfile rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }
}
