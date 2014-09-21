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
    int counter = 0;


    public TaggedFavoritesToUserProfile(UserProfile startUserProfile, UserProfile favoritesUserProfile, int counter) {
        this.startUserProfile               =    startUserProfile;
        this.favoritesUserProfile           =    favoritesUserProfile;
        this.counter                        =    counter;
    }

    public TaggedFavoritesToUserProfile() {

    }
}
