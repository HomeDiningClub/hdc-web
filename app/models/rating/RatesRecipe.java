package models.rating;

import models.Recipe;
import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
import services.InstancedServices;

@RelationshipEntity(type = RelationshipTypesJava.RATED_RECIPE.Constant)
public class RatesRecipe extends BaseRating {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public Recipe userRates;


    public UserCredential getUserWhoIsRating() {
        return InstancedServices.userCredentialService().fetchUserCredential(userWhoIsRating);
    }

    public Recipe getUserRates() {
        return InstancedServices.recipeService().fetchRecipe(userRates);
    }


    public RatesRecipe rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
