package models.profile;

import models.UserProfile;
import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity(type = "FAVORITE_USERS")
public class TaggedFavoritesToUserProfile extends AbstractEntity {

    @Fetch @StartNode   public UserProfile      startUserProfile;
    @Fetch @EndNode     public UserProfile      favoritesUserProfile;
    long dateTime = 0L;


    public TaggedFavoritesToUserProfile(UserProfile startUserProfile, UserProfile favoritesUserProfile, long dateTime) {
        this.startUserProfile               =    startUserProfile;
        this.favoritesUserProfile           =    favoritesUserProfile;
        this.dateTime                        =    dateTime;
    }

    public TaggedFavoritesToUserProfile() {

    }

    public long created() {
        return this.dateTime;
    }

}
