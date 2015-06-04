package models;
import interfaces.IEditable;
import models.content.ContentBase;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.UUID;

@NodeEntity
public class BlogPost extends ContentBase implements IEditable {

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "title")
    private String title;
    @Indexed(indexType = IndexType.FULLTEXT,indexName = "text")
    private String text;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.MAIN_IMAGE.Constant, direction = Direction.OUTGOING)
    private ContentFile mainImage;

    @RelatedTo(type = RelationshipTypesJava.HAS_BLOGPOSTS.Constant, direction = Direction.INCOMING)
    private UserProfile ownerProfile;

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

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return this.title;
    }

    public void setText(String text) {this.text = text;}

    public String getText() {
        return this.text;
    }


    public void  removeReferences() {
        deleteMainImage();
        if(this.ownerProfile != null) {
            this.ownerProfile = null;
        }

    }


}

