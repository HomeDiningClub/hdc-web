package models;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import models.profile.TagWord;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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


    public String fistName = "";

    public String lastName = "";

    public String userIdentity = "";

    public String providerIdentity = "";

    public String email = "";


    // Userid + "_" + providerId
    @Indexed(indexType = IndexType.LABEL)
    public String keyIdentity = "";

    @RelatedToVia(type = "TAGGED_ON")
    private Set<TaggedUserProfile> userProfileTag = new HashSet<>();

    public TaggedUserProfile tag(TagWord tagWord) {
        TaggedUserProfile taggedProfile = new TaggedUserProfile(this, tagWord);
        userProfileTag.add(taggedProfile);
        return taggedProfile;
    }

    public void removeAllTags() {

        Iterable<TaggedUserProfile> itter = getTags();


        if(itter != null) {

            // temporary store all tags
            ArrayList<TaggedUserProfile> arr = new ArrayList<TaggedUserProfile>();
            for (TaggedUserProfile tagProfile : itter) {
                arr.add(tagProfile);
            }

            // remove all tags
            Iterator<TaggedUserProfile> itter2 = (Iterator<TaggedUserProfile>) arr.iterator();
            while (itter2.hasNext()) {
                userProfileTag.remove(itter2.next());
            }
        }
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
