package models.like;

import models.Event;
import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = RelationshipTypesJava.LIKES_EVENT.Constant)
public class UserCredentialLikeEvent extends BaseLike {

    @StartNode
    public UserCredential userWhoLikes;

    @EndNode
    public Event userLikes;

    public UserCredential getUserWhoLikes() {
        if(userWhoLikes == null)
            throw new NullPointerException("getUserWhoLikes() in UserCredentialLikeEvent is null, you need to @Fetch");
        return userWhoLikes;
        //return InstancedServices.userCredentialService().fetchUserCredential(userWhoLikes);
    }

    public Event getUserLikes() {
        if(userLikes == null)
            throw new NullPointerException("getUserLikes() in UserCredentialLikeEvent is null, you need to @Fetch");
        return userLikes;
        //return InstancedServices.eventService().fetchEvent(userLikes);
    }

    public UserCredentialLikeEvent like(UserCredential userWhoLikes, Event userLikes, boolean likes, String userLikesIP) {
        this.userWhoLikes = userWhoLikes;
        this.userLikes = userLikes;
        this.likes = likes;
        this.userLikesIP = userLikesIP;
        return this;
    }

}
