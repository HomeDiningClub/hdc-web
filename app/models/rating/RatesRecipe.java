package models.rating;

import models.Recipe;
import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = RelationshipTypesJava.RATED_RECIPE.Constant)
public class RatesRecipe extends BaseRating {

    @StartNode
    @Fetch
    public UserCredential userWhoIsRating;

    @EndNode
    @Fetch
    public Recipe userRates;

    public UserCredential getUserWhoIsRating() {
        return this.userWhoIsRating;
    }

    public Recipe getUserRates() {
        return this.userRates;
    }

    public RatesRecipe rate(UserCredential userWhoIsRating, Recipe userRates, int ratingValue, String ratingComment, String userRaterIP) {
        this.userWhoIsRating = userWhoIsRating;
        this.userRates = userRates;
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
