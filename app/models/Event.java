package models;

import customUtils.Helpers;
import models.content.ContentBase;
import models.event.AlcoholServing;
import models.event.EventDate;
import models.event.MealType;
import models.files.ContentFile;
import models.like.UserCredentialLikeEvent;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import traits.IEditable;

import java.util.*;

@NodeEntity
public class Event extends ContentBase implements IEditable {

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "eventName")
    private String name;

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "eventPrice")
    private Long price;

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "eventpreAmble")
    private String preAmble;

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "eventMainBody")
    private String mainBody;

    private Integer maxNrOfGuests;
    private Integer minNrOfGuests;
    private Boolean childFriendly;
    private Boolean handicapFriendly;
    private Boolean havePets;
    private Boolean smokingAllowed;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.ALCOHOL_SERVING.Constant, direction = Direction.INCOMING)
    private AlcoholServing alcoholServing;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.MAIN_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile mainImage;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.IMAGES.Constant, direction = Direction.OUTGOING)
    private Set<ContentFile> eventImages;

    @RelatedTo(type = RelationshipTypesJava.HOSTS_EVENTS.Constant, direction = Direction.INCOMING)
    private UserProfile ownerProfile;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.MEAL_TYPE.Constant, direction = Direction.INCOMING)
    private MealType mealType;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.EVENT_TIMES.Constant, direction = Direction.OUTGOING)
    private Set<EventDate> eventDates;

    @Indexed(unique = true, indexType = IndexType.LABEL)
    private String eventLinkName = "";

    // Rating
    //@RelatedToVia(type = RelationshipTypesJava.RATED_RECIPE.Constant, direction = Direction.INCOMING)
    //private Set<RatesRecipe> ratings;

    // Like
    @RelatedToVia(type = RelationshipTypesJava.LIKES_EVENT.Constant, direction = Direction.INCOMING)
    private Set<UserCredentialLikeEvent> likes;



    // Getter & Setters
    public void setChildFriendly(Boolean value){
        this.childFriendly = value;
    }
    public Boolean getChildFriendly(){
        if(this.childFriendly == null)
            this.childFriendly = true;

        return this.childFriendly;
    }

    public void setHandicapFriendly(Boolean value){
        this.handicapFriendly = value;
    }
    public Boolean getHandicapFriendly(){
        if(this.handicapFriendly == null)
            this.handicapFriendly = true;

        return this.handicapFriendly;
    }

    public void setHavePets(Boolean value){
        this.havePets = value;
    }
    public Boolean getHavePets(){
        if(this.havePets == null)
            this.havePets = false;

        return this.havePets;
    }

    public void setSmokingAllowed(Boolean value){
        this.smokingAllowed = value;
    }
    public Boolean getSmokingAllowed(){
        if(this.smokingAllowed == null)
            this.smokingAllowed = false;

        return this.smokingAllowed;
    }

    public void setMaxNrOfGuests(Integer maxNr){
        this.maxNrOfGuests = maxNr;
    }

    public Integer getMaxNrOfGuests(){
        if(this.maxNrOfGuests == null)
            this.maxNrOfGuests = 0;

        return this.maxNrOfGuests;
    }

    public void setMinNrOfGuests(Integer minNr){
        this.minNrOfGuests = minNr;
    }

    public Integer getMinNrOfGuests(){
        if(this.minNrOfGuests == null)
            this.minNrOfGuests = 0;

        return this.minNrOfGuests;
    }

    public void setMainBody(String mainBody){
        this.mainBody = mainBody;
    }

    public String getMainBody(){
        if(this.mainBody == null)
            this.mainBody = "";
        return this.mainBody;
    }

    public void setPreAmble(String preAmble){
        this.preAmble = preAmble;
    }

    public String getPreAmble(){
        if(this.preAmble == null)
            this.preAmble = "";
        return this.preAmble;
    }

    public void setName(String name){
        this.name = name;
        setLink(name);
    }

    public Long getPrice() {
        return this.price;
    }

    public void setPrice(Long price){
        this.price = price;
    }

    public String getName() {
        return this.name;
    }

    public MealType setMealType(MealType type) {
        this.mealType = type;
        return this.mealType;
    }

    public MealType getMealType() {
        return this.mealType;
    }

    public AlcoholServing setAlcoholServing(AlcoholServing serving) {
        this.alcoholServing = serving;
        return this.alcoholServing;
    }

    public AlcoholServing getAlcoholServing() {
        return this.alcoholServing;
    }


    public Iterable<ContentFile> getEventImages() {
        return this.eventImages;
    }

    @Transient
    public Integer getMaxNrOfEventImages(){
        return 5;
    }

    public ContentFile addEventImage(ContentFile eventImage) {
        if(this.eventImages == null){
            this.eventImages = new HashSet<>();
        }

        this.eventImages.add(eventImage);
        return eventImage;
    }

    public ContentFile getMainImage() {
        return this.mainImage;
    }

    @Transient
    public Integer getMaxNrOfMainImages(){
        return 1;
    }

    public void setAndRemoveMainImage(ContentFile newMainImage) {
        // Remove former image before adding a new one
        deleteMainImage();
        this.mainImage = newMainImage;
    }

    public void deleteMainImage() {
        if(this.mainImage != null && this.mainImage.objectId != null)
        {
            this.mainImage = null;
        }
    }

    public void deleteEventImages() {
        if(this.eventImages != null && !this.eventImages.isEmpty())
        {
            // Temporary store all
            ArrayList<ContentFile> arr = new ArrayList<ContentFile>();
            for (ContentFile tagProfile : this.eventImages) {
                arr.add(tagProfile);
            }

            // Remove all
            Iterator<ContentFile> iteration = (Iterator<ContentFile>) arr.iterator();
            while (iteration.hasNext()) {
                deleteEventImage(iteration.next());
            }
        }
    }

    public void deleteLikes() {
        if(this.likes == null){
            this.likes = new HashSet<>();
        }else {
            this.likes.clear();
        }
    }

//    public void deleteRatings() {
//        if(this.ratings == null){
//            this.ratings = new HashSet<>();
//        }else {
//            this.ratings.clear();
//        }
//    }

    public void deleteEventImage(ContentFile eventImage) {
        if(this.eventImages == null){
            this.eventImages = new HashSet<>();
        }else {
            this.eventImages.remove(eventImage);
        }
    }

    private void setLink(String link) {
        this.eventLinkName = Helpers.createRoute(link) + "-" + this.objectId;
    }

    public String getLink() {
        if(this.eventLinkName == null || this.eventLinkName.isEmpty())
            setLink(this.name);

        return this.eventLinkName;
    }

    // Verify the object owner
    @Transient
    public Boolean isEditableBy(UUID objectId){
        if(objectId != null && objectId.equals(this.getOwnerProfile().getOwner().objectId))
            return true;
        else
            return false;
    }

    @Fetch
    public UserProfile getOwnerProfile() {
        return this.ownerProfile;
    }


    // Rate
    // Rating - average
//    public int getAverageRating() {
//        int sumOfRatingValues = 0, count = 0;
//        for (RatesRecipe rating : this.getRatings()) {
//            sumOfRatingValues += rating.ratingValue;
//            count++;
//        }
//        return count == 0 ? 0 : sumOfRatingValues / count;
//    }
//
//    // Rating count
//    public int getNrOfRatings() {
//        int count = 0;
//
//        if(this.getRatings() != null)
//            count = this.getRatings().size();
//
//        return count;
//    }
//
//    public void addRating(RatesRecipe ratingToAdd) {
//        if(this.ratings == null)
//            this.ratings = new HashSet<>();
//
//        this.ratings.add(ratingToAdd);
//    }

//    @Fetch
//    public Set<RatesRecipe> getRatings() {
//        if(this.ratings == null)
//            this.ratings = new HashSet<>();
//
//        return this.ratings;
//    }
//
//
//    // Like
    @Fetch
    public Set<UserCredentialLikeEvent> getLikes() {
        if(this.likes == null)
            this.likes = new HashSet<>();

        return this.likes;
    }

    public void addLike(UserCredentialLikeEvent likeToAdd) {
        if(this.likes == null)
            this.likes = new HashSet<>();

        this.likes.add(likeToAdd);
    }

    // Like count
    public int getNrOfLikes() {
        int count = 0;

        if(this.getLikes() != null)
            count = this.getLikes().size();

        return count;
    }



    // Event dates
    @Fetch
    public Set<EventDate> getEventDates() {
        if(this.eventDates == null)
            this.eventDates = new HashSet<>();

        return this.eventDates;
    }
    public void addEventDate(EventDate eventToAdd) {
        if(this.eventDates == null)
            this.eventDates = new HashSet<>();

        this.eventDates.add(eventToAdd);
    }
    public void modifyEventDate(EventDate eventToModify) {
        if(this.eventDates == null)
            this.eventDates = new HashSet<>();

         Optional<EventDate> optMathingEvent = this.eventDates.stream()
                .filter(c -> c.objectId.equals(eventToModify.objectId))
                .findFirst();

        if(optMathingEvent.isPresent()){
            optMathingEvent.get().setEventDateTime(eventToModify.getEventDateTime());
        }
    }
    public void deleteEventDate(EventDate eventDate) {
        if(this.eventDates == null){
            this.eventDates = new HashSet<>();
        }else {
            this.eventDates.remove(eventDate);
        }
    }
    public void deleteEventDates() {
        if(this.eventDates != null && !this.eventDates.isEmpty())
        {
            // Temporary store all
            ArrayList<EventDate> arr = new ArrayList<EventDate>();
            for (EventDate ed : this.eventDates) {
                arr.add(ed);
            }

            // Remove all
            Iterator<EventDate> iteration = (Iterator<EventDate>) arr.iterator();
            while (iteration.hasNext()) {
                deleteEventDate(iteration.next());
            }
        }
    }


    // Constructors
    public Event(String name, String preAmble, String mainBody, ContentFile mainImage, Set<ContentFile> eventImages){
        this.setName(name);
        this.mainImage = mainImage;
        this.preAmble = preAmble;
        this.eventImages = eventImages;
        this.mainBody = mainBody;
    }

    public Event(String name, String preAmble, String mainBody){
        this.setName(name);
        this.preAmble = preAmble;
        this.mainBody = mainBody;
        this.eventImages = new HashSet<>();
    }

    public Event(String name, String mainBody){
        this.setName(name);
        this.mainBody = mainBody;
        this.eventImages = new HashSet<>();
    }

    public Event(String name){
        this.setName(name);
        this.eventImages = new HashSet<>();
    }

    protected Event(){
        this.eventImages = new HashSet<>();
    }
}
