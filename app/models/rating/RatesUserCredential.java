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
    public UserCredential userWhoIsRating;

    @EndNode
    public UserCredential userRates;

    public UserCredential getUserWhoIsRating() {
        if(userWhoIsRating == null)
            throw new NullPointerException("getUserWhoIsRating() in RatesUserCredential is null, you need to @Fetch");
        return this.userWhoIsRating;
    }

    public UserCredential getUserRates() {
        if(userRates == null)
            throw new NullPointerException("getUserRates() in RatesUserCredential is null, you need to @Fetch");
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
