package models.like;

import models.Recipe;
import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
import services.InstancedServices;

@RelationshipEntity(type = RelationshipTypesJava.LIKES_RECIPE.Constant)
public class UserCredentialLikeRecipe extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public Recipe userLikes;

    public UserCredential getUserWhoLikes() {
        return InstancedServices.userCredentialService().fetchUserCredential(userWhoLikes);
    }

    public Recipe getUserLikes() {
        return InstancedServices.recipeService().fetchRecipe(userLikes);
    }


    public UserCredentialLikeRecipe like(UserCredential userWhoLikes, Recipe userLikes, boolean likes, String userLikesIP) {
        this.userWhoLikes = userWhoLikes;
        this.userLikes = userLikes;
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
