package models.profile;

import models.UserProfile;
import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = "TAGGED_ON")
public class TaggedUserProfile extends AbstractEntity {

    @StartNode
    public UserProfile userProfile;

    @EndNode
    public TagWord tagWord;

    public TaggedUserProfile(UserProfile userProfile, TagWord tagWord) {
        this.userProfile = userProfile;
        this.tagWord = tagWord;
    }

    public TaggedUserProfile() {

    }
}
