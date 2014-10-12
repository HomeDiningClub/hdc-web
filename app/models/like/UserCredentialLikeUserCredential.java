package models.like;

import models.UserCredential;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity
public class UserCredentialLikeUserCredential extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public UserCredential userLikes;

    public UserCredentialLikeUserCredential like(boolean likes, String userLikesIP) {
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
