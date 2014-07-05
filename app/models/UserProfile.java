package models;


import models.base.AbstractEntity;
import models.profile.TagWord;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import scala.collection.mutable.HashSet;

import java.util.Set;
import java.util.UUID;

@NodeEntity
public class UserProfile extends AbstractEntity {

    @RelatedToVia(type = "USERPROFILE_TAGWORD_OF", direction = Direction.OUTGOING)
    public Set<TaggedUserProfile> userTags;

/*
    @Fetch
    @RelatedTo(type = "PROFILE_CREDENTIAL", direction = Direction.BOTH)
    public UserCredential credential;
*/
  //  @Fetch
  //  @RelatedTo(type = "PROFILE_LOCATION", direction = Direction.OUTGOING)
  //  public UserCredential locations;


    public String group = "";

    // Platinum premim
    // Gold
    // Silver
    // Admin

    public boolean isHost = false;

    public String aboutMe = "";

    public String profilePicture = "";

    public String backgroundImage = "";

   // @Indexed(indexName = "TEST_USERPROFILE_HDC_KEY")
    public UUID key;

    public String userId            = "";
    public String providerId        = "";

    // food pictures

    public TaggedUserProfile memberOf(TagWord tag) {
        TaggedUserProfile userTag = new TaggedUserProfile(this, tag);

        if(userTags != null && userTag != null) {
            userTags.add(userTag);
        }

        return userTag;
    }

    public TaggedUserProfile remove(TaggedUserProfile userTag) {


        boolean b = userTags.remove(userTag);

        System.out.println("svar : " + b);

        return userTag;
    }



    public Iterable<TaggedUserProfile> getUserProfileTags() { return userTags; }


    public UserProfile() {
        this.key = UUID.randomUUID();
    }

    public  UserProfile(String email) {
        this();
    }


}
