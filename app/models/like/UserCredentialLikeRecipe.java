package models.like;

import models.Recipe;
import models.UserCredential;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity
public class UserCredentialLikeRecipe extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public Recipe userLikes;

    public UserCredentialLikeRecipe like(boolean likes, String userLikesIP) {
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
