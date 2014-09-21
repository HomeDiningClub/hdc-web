package models.files;

import models.UserCredential;
import java.util.HashSet;
import java.util.Set;

import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import services.InstancedServices;

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

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.OWNER.Constant, direction = Direction.BOTH)
    public UserCredential owner;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.OUTGOING)
    public Set<FileTransformation> fileTransformations;

    // Getter Setters
    public String getBasePath(){
        if(this.isAdminFile == null)
            this.isAdminFile = false;
        return InstancedServices.contentFileService().getFilePath(this,this.isAdminFile);
    }

    public String getUrl(){
        return InstancedServices.contentFileService().getBucketUrl(this);
    }

    public FileTransformation getTransformByName(String name)
    {
        for (FileTransformation transform : this.fileTransformations) {
            if(transform.name.equalsIgnoreCase(name))
                return transform;
        }
        return null;
    }


    // Constructors
    public ContentFile(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser, Set<FileTransformation> fileTransforms, Boolean isAdminFile) {
        this.fileTransformations = fileTransforms;
        populateBaseData(name, extension, contentType, baseContentType, ownerUser, isAdminFile);
    }

    public ContentFile(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser, Boolean isAdminFile) {
        this.fileTransformations = new HashSet<>();
        populateBaseData(name, extension, contentType, baseContentType, ownerUser, isAdminFile);
    }

    public ContentFile(String name, String extension, String contentType, String baseContentType, Boolean isAdminFile) {
        this.fileTransformations = new HashSet<>();
        populateBaseData(name, extension, contentType, baseContentType, null, isAdminFile);
    }

    protected ContentFile() {
        this.fileTransformations = new HashSet<>();
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
