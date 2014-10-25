package models.like;

import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;
import services.InstancedServices;

@RelationshipEntity(type = RelationshipTypesJava.LIKES_USER.Constant)
public class UserCredentialLikeUserCredential extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public UserCredential userLikes;

    public UserCredential getUserWhoLikes() {
        return InstancedServices.userCredentialService().fetchUserCredential(userWhoLikes);
    }

    public UserCredential getUserLikes() {
        return InstancedServices.userCredentialService().fetchUserCredential(userLikes);
    }

    public UserCredentialLikeUserCredential like(UserCredential userWhoLikes, UserCredential userLikes, boolean likes, String userLikesIP) {
        this.userWhoLikes = userWhoLikes;
        this.userLikes = userLikes;
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
