package models.profile;

import models.UserProfile;
import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;

@RelationshipEntity(type = "PROFILE_TAGGED_AS")
public class TaggedUserProfile  extends AbstractEntity {

    @Fetch @StartNode   public UserProfile userProfile;
    @Fetch @EndNode     public TagWord     tagWord;

    public TaggedUserProfile(UserProfile userProfile, TagWord tagWord) {
        this.userProfile    = userProfile;
        this.tagWord        = tagWord;
    }

    public TaggedUserProfile() {

    }
}
