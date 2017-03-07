package models;

import models.base.AuditEntity;
import models.event.BookedEventDate;
import models.event.EventDate;
import models.files.ContentFile;
import models.location.County;
import models.modelconstants.RelationshipTypesJava;
import models.modelconstants.UserLevelJava;
import models.profile.TagWord;
import models.profile.TaggedFavoritesToUserProfile;
import models.profile.TaggedLocationUserProfile;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import traits.IEditable;

import java.util.*;

@NodeEntity
public class UserProfile extends AuditEntity implements IEditable {

    /*
    public String county = "";
    public String childFfriendly    = "";
    public String handicapFriendly  = "";
    public String havePets          = "";
    public String smoke             = "";
    public String allkoholServing   = "";
    public String maxNoOfGuest      = "";
    public String minNoOfGuest      = "";
    public String firstName          = "";
    public String lastName          = "";
    public String email             = "";
    */

    public boolean isTermsOfUseApprovedAccepted = false;
    public boolean payCash           = false;
    public boolean paySwish          = false;
    public boolean payBankCard       = false;
    public boolean payIZettle        = false;
    public String userIdentity       = "";
    public String providerIdentity   = "";
    public String streetAddress      = "";
    public String zipCode            = "";
    public String city               = "";
    public String phoneNumber        = "";
    public String aboutMe = "";
    public String aboutMeHeadline = "";

    // Profile look up name
    @Indexed(unique = true, indexType = IndexType.LABEL)
    public String profileLinkName = "";

    // Userid + "_" + providerId
    @Indexed(indexType = IndexType.LABEL)
    public String keyIdentity = "";

    @RelatedToVia(type = "TAGGED_ON")
    private Set<TaggedUserProfile> userProfileTag;

    private Set<String> role;

    @RelatedToVia(type = "LOCATION_AT")
    @Fetch
    private Set<TaggedLocationUserProfile> userLocationProfileTag;

    @RelatedToVia(type="FAVORITE_USER")
    private Set<TaggedFavoritesToUserProfile> userFriendsProfileTag;

    @RelatedToVia(type = RelationshipTypesJava.BOOKED_EVENT_DATE.Constant, direction = Direction.OUTGOING)
    private Set<BookedEventDate> bookedEventDates;

    @RelatedTo(type = RelationshipTypesJava.HAS_RECIPES.Constant, direction = Direction.OUTGOING)
    private Set<Recipe> recipes;

    @RelatedTo(type = RelationshipTypesJava.HAS_BLOGPOSTS.Constant, direction = Direction.OUTGOING)
    private Set<BlogPost> blogPosts;

    @RelatedTo(type = RelationshipTypesJava.HOSTS_EVENTS.Constant, direction = Direction.OUTGOING)
    private Set<Event> events;

    @RelatedTo(type = "IN_PROFILE", direction = Direction.INCOMING)
    private UserCredential owner;

    @RelatedTo(type = "IN_USER_VISITED", direction = Direction.OUTGOING)
    private ViewedByMember memberVisited;

    @RelatedTo(type = "IN_OTHER_VISITED", direction = Direction.OUTGOING)
    private ViewedByUnKnown unKnownVisited;


    @RelatedTo(type = RelationshipTypesJava.MAIN_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile mainImage;

    @RelatedTo(type = RelationshipTypesJava.AVATAR_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile avatarImage;

    public ViewedByMember getmemberVisited() {
        return this.memberVisited;
    }
    public void setViewedByMeber(ViewedByMember viewed) {
        this.memberVisited = viewed;
    }
    public void removeViewedByMember() {
        if(this.memberVisited != null && this.memberVisited.objectId != null) {
            this.memberVisited = null;
        }
    }

    public ViewedByUnKnown getUnKnownVisited() {
        return this.unKnownVisited;
    }

    public void setViewedByUnKnown(ViewedByUnKnown viewed) {
        this.unKnownVisited = viewed;
    }

    public void removeViewedByUnKnown() {
        if(this.unKnownVisited != null && this.unKnownVisited.objectId != null) {
            this.unKnownVisited = null;
        }
    }



    @Fetch
    public Set<String> getRole() {

        if(this.role == null) {
            this.role = new HashSet<>();
            role.add(UserLevelJava.GUEST.Constant);
            role.add(UserLevelJava.HOST.Constant);
        }

        return this.role;
    }

    public void setHost(Set<String> role) {
        this.role = role;
    }

    public boolean isUserHost() {
        return role != null && (role.contains(UserLevelJava.GUEST.Constant) && role.contains(UserLevelJava.HOST.Constant));
    }

    public String roleChecked(String r) {

        if (role.contains(r))
            return "checked";
        else
            return  "";
    }

    public boolean hasPaymentOptionSelected() {
        return payBankCard || payCash || payIZettle || paySwish;
    }

    @Fetch
    public ContentFile getMainImage() {
        return this.mainImage;
    }

    @Transient
    public Integer getMaxNrOfMainImages(){
        return 1;
    }

    public void setAndRemoveMainImage(ContentFile newImage) {
        // Remove former image before adding a new one
        deleteMainImage();
        this.mainImage = newImage;
    }

    public void deleteMainImage() {
        if(this.mainImage != null && this.mainImage.objectId != null)
        {
            //InstancedServices.contentFileService().deleteFile(this.mainImage.objectId);
            this.mainImage = null;
        }
    }

    @Fetch
    public ContentFile getAvatarImage() {
        return this.avatarImage;
    }

    @Transient
    public Integer getMaxNrOfAvatarImages(){
        return 1;
    }

    public void setAndRemoveAvatarImage(ContentFile newImage) {
        // Remove former image before adding a new one
        deleteAvatarImage();
        this.avatarImage = newImage;
    }

    public void deleteAvatarImage() {
        if(this.avatarImage != null && this.avatarImage.objectId != null)
        {
            this.avatarImage = null;
        }
    }

    public BookedEventDate addBookingToEvent(EventDate eventDate, Integer nrOfGuestsToBeBooked, String comment) {
        BookedEventDate bookedEvent = new BookedEventDate(this, nrOfGuestsToBeBooked, eventDate, comment);
        bookedEventDates.add(bookedEvent);
        return bookedEvent;
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


    // Remove favorite
    public void removeFavoriteUserProfile(TaggedFavoritesToUserProfile userProfile) {
        userFriendsProfileTag.remove(userProfile);
    }


    // Add favorite
    public TaggedFavoritesToUserProfile addFavoriteUserProfile(UserProfile userProfile) {

        Calendar cal = Calendar.getInstance();


        TaggedFavoritesToUserProfile taggedFavoritesToUserProfile = new
                TaggedFavoritesToUserProfile(this, userProfile,
                cal.getTimeInMillis());
       userFriendsProfileTag.add(taggedFavoritesToUserProfile);

        // check if it is not to many favorites when remove the oldest ...
        if(userFriendsProfileTag.size() > 30) {

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

    public UserProfile addBlogPosts(BlogPost addedBlogPosts) {
        if(this.blogPosts == null)
            this.blogPosts = new HashSet<BlogPost>();

        this.blogPosts.add(addedBlogPosts);
        return this;
    }

    public UserProfile addEvent(Event eventToAdd) {
        if(this.events == null)
            this.events = new HashSet<Event>();

        this.events.add(eventToAdd);
        return this;
    }

    public UserProfile removeEvent(Event addedEvent) {
        if(this.events == null)
            this.events = new HashSet<Event>();

        this.events.remove(addedEvent);
        return this;
    }

    public UserProfile removeBlogPosts(BlogPost addedBlogPosts) {
        if(this.blogPosts == null)
            this.blogPosts = new HashSet<BlogPost>();

        this.blogPosts.remove(addedBlogPosts);
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
            Iterator<TaggedUserProfile> itter2 = arr.iterator();
            while (itter2.hasNext()) {
                userProfileTag.remove(itter2.next());
            }
        }
    }

    @Transient
    public Boolean isEditableBy(UUID objectId){
        return objectId != null && objectId.equals(this.getOwner().objectId);
    }

    @Fetch
    public UserCredential getOwner() { return owner; }

    public Iterable<Recipe> getRecipes() { return recipes; }
    public Iterable<TaggedUserProfile> getTags() { return userProfileTag; }
    public Iterable<TaggedLocationUserProfile> getLocations() { return userLocationProfileTag; }
    public Iterable<BookedEventDate> getBookedEventDates() { return bookedEventDates; }
    public Iterable<BlogPost> getBlogPosts() { return blogPosts; }
    public Set<TaggedFavoritesToUserProfile> getFavorites() { return userFriendsProfileTag;}


    public UserProfile() {

    }


}
