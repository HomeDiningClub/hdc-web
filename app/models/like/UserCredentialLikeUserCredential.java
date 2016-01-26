package models.like;

import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = RelationshipTypesJava.LIKES_USER.Constant)
public class UserCredentialLikeUserCredential extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public UserCredential userLikes;

    public UserCredential getUserWhoLikes() {
        if(userWhoLikes == null)
            throw new NullPointerException("getUserWhoLikes() in UserCredentialLikeUserCredential is null, you need to @Fetch");
        return userWhoLikes;
        //return InstancedServices.userCredentialService().fetchUserCredential(userWhoLikes);
    }

    public UserCredential getUserLikes() {
        if(userLikes == null)
            throw new NullPointerException("getUserLikes() in UserCredentialLikeUserCredential is null, you need to @Fetch");
        return userLikes;
        //return InstancedServices.userCredentialService().fetchUserCredential(userLikes);
    }

    public UserCredentialLikeUserCredential like(UserCredential userWhoLikes, UserCredential userLikes, boolean likes, String userLikesIP) {
        this.userWhoLikes = userWhoLikes;
        this.userLikes = userLikes;
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
