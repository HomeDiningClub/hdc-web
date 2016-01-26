package models.like;

import models.Recipe;
import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = RelationshipTypesJava.LIKES_RECIPE.Constant)
public class UserCredentialLikeRecipe extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public Recipe userLikes;

    public UserCredential getUserWhoLikes() {
        if(userWhoLikes == null)
            throw new NullPointerException("getUserWhoLikes() in UserCredentialLikeRecipe is null, you need to @Fetch");
        return userWhoLikes;
        //return InstancedServices.userCredentialService().fetchUserCredential(userWhoLikes);
    }

    public Recipe getUserLikes() {
        if(userLikes == null)
            throw new NullPointerException("getUserLikes() in UserCredentialLikeRecipe is null, you need to @Fetch");
        return userLikes;
        //return InstancedServices.recipeService().fetchRecipe(userLikes);
    }


    public UserCredentialLikeRecipe like(UserCredential userWhoLikes, Recipe userLikes, boolean likes, String userLikesIP) {
        this.userWhoLikes = userWhoLikes;
        this.userLikes = userLikes;
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
