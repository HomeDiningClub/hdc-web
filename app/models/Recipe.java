package models;

import interfaces.IEditable;
import models.content.ContentBase;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import models.rating.RatesRecipe;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import services.InstancedServices;
import utils.Helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;
import java.lang.Boolean;

@NodeEntity
public class Recipe extends ContentBase implements IEditable {

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "recipeName")
    private String name;
    private String preAmble;
    @Indexed(indexType = IndexType.FULLTEXT,indexName = "recipeMainBody")
    private String mainBody;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.MAIN_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile mainImage;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.IMAGES.Constant, direction = Direction.OUTGOING)
    private Set<ContentFile> recipeImages;

    @RelatedTo(type = RelationshipTypesJava.HAS_RECIPES.Constant, direction = Direction.INCOMING)
    private UserProfile ownerProfile;

    @Indexed(unique = true, indexType = IndexType.LABEL)
    private String recipeLinkName = "";

    // Rating
    @RelatedToVia(type = RelationshipTypesJava.RATED_RECIPE.Constant, direction = Direction.INCOMING)
    private Set<RatesRecipe> ratings;


    // Getter & Setters
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

    public Iterable<ContentFile> getRecipeImages() {
        return this.recipeImages;
    }

    public ContentFile addRecipeImage(ContentFile recipeImage) {
        if(this.recipeImages == null){
            this.recipeImages = new HashSet<>();
        }

        this.recipeImages.add(recipeImage);
        return recipeImage;
    }

    public ContentFile getMainImage() {
        return this.mainImage;
    }

    public void setAndRemoveMainImage(ContentFile newMainImage) {

        // Remove former image before adding a new one
        deleteMainImage();

        this.mainImage = newMainImage;
    }

    public void deleteMainImage() {
        if(this.mainImage != null && this.mainImage.objectId != null)
        {
            InstancedServices.contentFileService().deleteFile(this.mainImage.objectId);
            this.mainImage = null;
        }
    }

    public void deleteRecipeImages() {
        if(this.recipeImages != null && !this.recipeImages.isEmpty())
        {
            // Temporary store all
            ArrayList<ContentFile> arr = new ArrayList<ContentFile>();
            for (ContentFile tagProfile : this.recipeImages) {
                arr.add(tagProfile);
            }

            // Remove all
            Iterator<ContentFile> iteration = (Iterator<ContentFile>) arr.iterator();
            while (iteration.hasNext()) {
                deleteRecipeImage(iteration.next());
            }
        }
    }

    public void deleteRecipeImage(ContentFile recipeImage) {
        if(this.recipeImages == null){
            this.recipeImages = new HashSet<>();
        }else {
            InstancedServices.contentFileService().deleteFile(recipeImage.objectId);
            this.recipeImages.remove(recipeImage);
        }
    }


    public String getName() {
        return this.name;
    }

    private void setLink(String link) {
        this.recipeLinkName = Helpers.createRoute(link) + "-" + this.objectId;
    }

    public String getLink() {
        if(this.recipeLinkName == null || this.recipeLinkName.isEmpty())
            setLink(this.name);

        return this.recipeLinkName;
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


    // Rate Recipe
    // Rating - average
    public int getAverageRating() {
        int sumOfRatingValues = 0, count = 0;
        for (RatesRecipe rating : this.getRatings()) {
            sumOfRatingValues += rating.ratingValue;
            count++;
        }
        return count == 0 ? 0 : sumOfRatingValues / count;
    }

    // Rating count
    public int getNrOfRatings() {
        int count = 0;

        if(this.getRatings() != null)
            count = this.getRatings().size();

        return count;
    }

    public void addRating(RatesRecipe ratingToAdd) {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        this.ratings.add(ratingToAdd);
    }

    @Fetch
    public Set<RatesRecipe> getRatings() {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        return this.ratings;
    }


    // Constructors
    public Recipe(String name, String preAmble, String mainBody, ContentFile mainImage, Set<ContentFile> recipeImages){
        this.setName(name);
        this.mainImage = mainImage;
        this.preAmble = preAmble;
        this.recipeImages = recipeImages;
        this.mainBody = mainBody;
    }

    public Recipe(String name, String preAmble, String mainBody){
        this.setName(name);
        this.preAmble = preAmble;
        this.mainBody = mainBody;
        this.recipeImages = new HashSet<>();
    }

    public Recipe(String name, String mainBody){
        this.setName(name);
        this.mainBody = mainBody;
        this.recipeImages = new HashSet<>();
    }

    public Recipe(String name){
        this.setName(name);
        this.recipeImages = new HashSet<>();
    }

    protected Recipe(){
        this.recipeImages = new HashSet<>();
    }
}
