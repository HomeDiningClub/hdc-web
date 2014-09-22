package models;

import interfaces.IEditable;
import models.base.AuditEntity;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import models.profile.TagWord;
import models.location.County;
import models.profile.TaggedFavoritesToUserProfile;
import models.profile.TaggedLocationUserProfile;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import services.InstancedServices;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.lang.Boolean;
import java.util.Calendar;

@NodeEntity
public class UserProfile extends AuditEntity implements IEditable {

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


    // Profile look up name
    @Indexed(unique = true, indexType = IndexType.LABEL)
    public String profileLinkName = "";


    // Userid + "_" + providerId
    @Indexed(indexType = IndexType.LABEL)
    public String keyIdentity = "";

    @RelatedToVia(type = "TAGGED_ON")
    private Set<TaggedUserProfile> userProfileTag;

    @RelatedToVia(type = "LOCATION_AT")
    private Set<TaggedLocationUserProfile> userLocationProfileTag;

    @RelatedToVia(type="FAVORITE_USER")
    private Set<TaggedFavoritesToUserProfile>userFriendsProfileTag;

    @RelatedTo(type = RelationshipTypesJava.HAS_RECIPES.Constant, direction = Direction.OUTGOING)
    private Set<Recipe> recipes;

    @RelatedTo(type = "IN_PROFILE", direction = Direction.INCOMING)
    private UserCredential owner;

    @RelatedTo(type = RelationshipTypesJava.MAIN_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile mainImage;

    @RelatedTo(type = RelationshipTypesJava.AVATAR_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile avatarImage;


    @Fetch
    public ContentFile getMainImage() {
        return this.mainImage;
    }

    public void setAndRemoveMainImage(ContentFile newImage) {
        // Remove former image before adding a new one
        deleteMainImage();
        this.mainImage = newImage;
    }

    public void deleteMainImage() {
        if(this.mainImage != null && this.mainImage.objectId != null)
        {
            InstancedServices.contentFileService().deleteFile(this.mainImage.objectId);
            this.mainImage = null;
        }
    }

    @Fetch
    public ContentFile getAvatarImage() {
        return this.avatarImage;
    }

    public void setAndRemoveAvatarImage(ContentFile newImage) {
        // Remove former image before adding a new one
        deleteAvatarImage();
        this.avatarImage = newImage;
    }

    public void deleteAvatarImage() {
        if(this.avatarImage != null && this.avatarImage.objectId != null)
        {
            InstancedServices.contentFileService().deleteFile(this.avatarImage.objectId);
            this.avatarImage = null;
        }
    }


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

    // User favorites
    public TaggedFavoritesToUserProfile addFavoriteUserProfile(UserProfile userProfile) {

        Calendar cal = Calendar.getInstance();


        TaggedFavoritesToUserProfile taggedFavoritesToUserProfile = new
                TaggedFavoritesToUserProfile(this, userProfile,
                cal.get(Calendar.MILLISECOND));
       userFriendsProfileTag.add(taggedFavoritesToUserProfile);

        // check if it is not to many favorites when remove the oldest ...
        if(userFriendsProfileTag.size() > 10) {

        Iterator<TaggedFavoritesToUserProfile> itter = userFriendsProfileTag.iterator();
         TaggedFavoritesToUserProfile startTag = null;
         TaggedFavoritesToUserProfile curTag = null;

            while(itter.hasNext()) {

                curTag = itter.next();

                if(startTag == null) {
                   startTag = curTag;
                }

                // Find the oldest favorite
                if(curTag.created() < startTag.created()) {
                    startTag = curTag;
                }
            }

            // remove the oldes favorite
            userFriendsProfileTag.remove(startTag);


        } // more when 10 favorites reomove one


        return taggedFavoritesToUserProfile;
    }


    //@TODO not working
    public TaggedFavoritesToUserProfile removeFavoriteUserProfile(UserProfile userProfile) {
        TaggedFavoritesToUserProfile taggedFavoritesToUserProfile = new TaggedFavoritesToUserProfile(this, userProfile, 1);
        userFriendsProfileTag.remove(taggedFavoritesToUserProfile);
        return taggedFavoritesToUserProfile;
    }



    public UserProfile addRecipe(Recipe recipeToAdd) {
        if(this.recipes == null)
            this.recipes = new HashSet<Recipe>();

        this.recipes.add(recipeToAdd);
        return this;
    }

    public void removeLocation() {
        userLocationProfileTag.clear();
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

    @Transient
    public Boolean isEditableBy(UUID objectId){
        if(objectId != null && objectId.equals(this.getOwner().objectId))
            return true;
        else
            return false;
    }

    @Fetch
    public UserCredential getOwner() { return owner; }

    @Fetch
    public Iterable<Recipe> getRecipes() { return recipes; }
    public Iterable<TaggedUserProfile> getTags() { return userProfileTag; }
    public Iterable<TaggedLocationUserProfile> getLocations() { return userLocationProfileTag; }

    public Set<TaggedFavoritesToUserProfile> getFavorites() { return userFriendsProfileTag;}

   // public boolean isHost = false;
    public String aboutMe = "";
    public String aboutMeHeadline = "";

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
