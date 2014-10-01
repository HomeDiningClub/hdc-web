package models.rating;

import models.Recipe;
import models.UserCredential;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity
public class RatesRecipe extends BaseRating {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public Recipe userRates;

    public RatesRecipe rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
