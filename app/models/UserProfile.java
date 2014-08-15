package models;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import models.profile.TagWord;
import models.location.County;
import models.profile.TaggedLocationUserProfile;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import models.modelconstants.RelationshipTypesJava;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@NodeEntity
public class UserProfile extends AbstractEntity {

  public String county = "";


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

    // Street address
    public String streetAddress = "";

    // Postcode or Zip-code
    public String zipCode       = "";

    // City
    public String city          = "";

    // Phonenumber
    public String phoneNumber = "";


    // profile look up name
    //@Indexed(unique=true)
    public String profileLinkName = "";


    // Userid + "_" + providerId
    @Indexed(indexType = IndexType.LABEL)
    public String keyIdentity = "";

    @RelatedToVia(type = "TAGGED_ON")
    private Set<TaggedUserProfile> userProfileTag;

    @RelatedToVia(type = "LOCATION_AT")
    private Set<TaggedLocationUserProfile> userLocationProfileTag;

    @RelatedTo(type = RelationshipTypesJava.HAS_RECIPES.Constant, direction = Direction.OUTGOING)
    private Set<Recipe> recipes;

    @Fetch
    @RelatedTo(type = "IN_PROFILE", direction = Direction.INCOMING)
    private UserCredential owner;

    public TaggedUserProfile tag(TagWord tagWord) {
        TaggedUserProfile taggedProfile = new TaggedUserProfile(this, tagWord);
        userProfileTag.add(taggedProfile);
        return taggedProfile;
    }

    public TaggedLocationUserProfile locate(County county) {
        TaggedLocationUserProfile taggedLocationProfile = new TaggedLocationUserProfile(this, county);
        userLocationProfileTag.add(taggedLocationProfile);
        return taggedLocationProfile;
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

    public UserCredential getOwner() { return owner; }
    public Iterable<Recipe> getRecipes() { return recipes; }
    public Iterable<TaggedUserProfile> getTags() { return userProfileTag; }
    public Iterable<TaggedLocationUserProfile> getLocations() { return userLocationProfileTag; }

   // public boolean isHost = false;
    public String aboutMe = "";
    public String aboutMeHeadline = "";
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
