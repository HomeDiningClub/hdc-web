package models.files;

import models.UserCredential;
import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class ContentFile extends AuditEntity {

    @Indexed
    public String name;
    public String extension;
    @Indexed
    public String contentType;
    // Eg: "VIDEO", "IMAGE", "CODE", "TEXT" etc..
    // See definition in enums.FileTypeEnums
    public String baseContentType;
    @Indexed
    public Boolean isAdminFile;
    @Indexed
    private String storeId;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.OWNER.Constant, direction = Direction.BOTH)
    public UserCredential owner;

//    @Fetch
//    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.OUTGOING)
//    public Set<FileTransformation> fileTransformations;

    // Getter Setters
/*
    public String getBasePath(){
        if(this.isAdminFile == null)
            this.isAdminFile = false;
        return InstancedServices.contentFileService().getFilePath(this,this.isAdminFile);
    }
*/
    public void setStoreId(String storeId){
        this.storeId = storeId;
    }

    public String getStoreId(){
        return this.storeId;
    }

    // Constructors
    public ContentFile(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser, Boolean isAdminFile) {
        populateBaseData(name, extension, contentType, baseContentType, ownerUser, isAdminFile);
    }

    public ContentFile(String name, String extension, String contentType, String baseContentType, Boolean isAdminFile) {
        populateBaseData(name, extension, contentType, baseContentType, null, isAdminFile);
    }

    protected ContentFile() {
        this.isAdminFile = false;
    }

    // Helpers
    private void populateBaseData(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser, Boolean isAdminFile) {
        this.baseContentType = baseContentType;
        this.owner = ownerUser;
        this.name = name;
        this.contentType = contentType;
        this.extension = extension;
        this.isAdminFile = isAdminFile;
    }
}
