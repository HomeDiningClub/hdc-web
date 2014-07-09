package models;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import models.profile.TagWord;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.Set;

@NodeEntity
public class UserProfile extends AbstractEntity {

/*
    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_CREDENTIAL.Constant, direction = Direction.BOTH)
    public UserCredential credential;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_LOCATION.Constant, direction = Direction.OUTGOING)
    public UserCredential locations;
*/

 //   public String group = "";

 /*
    //@Indexed(indexType = IndexType.FULLTEXT, indexName = "userprofile_userid")
    public String userId = "";

   // @Indexed(indexType = IndexType.FULLTEXT, indexName = "userprofile_providerid")
    public String providerId = "";
*/



    @RelatedToVia(type = "PROFILE_TAGGED_AS")
    private Set<TaggedUserProfile> userProfileTag;

    public TaggedUserProfile tag(TagWord tagWord) {
        TaggedUserProfile taggedProfile = new TaggedUserProfile(this, tagWord);
        userProfileTag.add(taggedProfile);
        return taggedProfile;
    }

    public TaggedUserProfile unTag(TagWord tagWord) {
        TaggedUserProfile taggedProfile = new TaggedUserProfile(this, tagWord);
        userProfileTag.remove(taggedProfile);
        return taggedProfile;
    }


    public Iterable<TaggedUserProfile> getTags() { return userProfileTag; }


    // Platinum premim
    // Gold
    // Silver
    // Admin
 //   public Integer memberStatus = 0;

   // public boolean isHost = false;

    public String aboutMe = "";

    public String profilePicture = "";

    public String backgroundImage = "";

    // food pictures

    public UserProfile() {

    }

/*
    public UserProfile(String userId, String providerId){
        this.userId     = userId;
        this.providerId = providerId;
    }
*/
}
