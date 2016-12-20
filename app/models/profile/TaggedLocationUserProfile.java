package models.profile;

import models.UserProfile;
import models.location.County;

import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = "LOCATION_AT")
public class TaggedLocationUserProfile extends AbstractEntity {

    @Fetch
    @StartNode
    public UserProfile userProfile;

    @Fetch
    @EndNode
    public County county;


    public TaggedLocationUserProfile(UserProfile userProfile, County county) {
        this.userProfile = userProfile;
        this.county = county;
    }

    public TaggedLocationUserProfile() {

    }

}
