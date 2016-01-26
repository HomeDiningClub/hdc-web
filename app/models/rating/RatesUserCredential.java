package models.rating;

import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = RelationshipTypesJava.RATED_USER.Constant)
public class RatesUserCredential extends BaseRating {

    @StartNode
    @Fetch
    public UserCredential userWhoIsRating;

    @EndNode
    @Fetch
    public UserCredential userRates;

    public UserCredential getUserWhoIsRating() {
        return this.userWhoIsRating;
    }

    public UserCredential getUserRates() {
        return this.userRates;
    }

    public RatesUserCredential rate(UserCredential userWhoIsRating, UserCredential userRates, int ratingValue, String ratingComment, String userRaterIP) {
        this.userWhoIsRating = userWhoIsRating;
        this.userRates = userRates;
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
