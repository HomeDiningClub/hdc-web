package models.files;

import models.UserCredential;
import models.base.AbstractEntity;
import java.util.HashSet;
import java.util.Set;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class ContentFile extends AbstractEntity {

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    @Indexed
    public String name;
    public String extension;
    public String contentType;
    // Eg: "VIDEO", "IMAGE", "CODE", "TEXT" etc..
    // See definition in enums.FileTypeEnums
    public String baseContentType;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.OWNER.Constant, direction = Direction.BOTH)
    public UserCredential owner;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.OUTGOING)
    public Set<FileTransformation> fileTransformations;

    @Transient
    public String url;

    @Transient
    public String basePath;


    // Getter Setters
    public FileTransformation getTransformByName(String name)
    {
        for (FileTransformation transform : this.fileTransformations) {
            if(transform.name.equalsIgnoreCase(name))
                return transform;
        }
        return null;
    }

    // Constructors
    public ContentFile(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser, Set<FileTransformation> fileTransforms) {
        this.fileTransformations = fileTransforms;
        populateBaseData(name, extension, contentType, baseContentType, ownerUser);
    }

    public ContentFile(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser) {
        this.fileTransformations = new HashSet<>();
        populateBaseData(name, extension, contentType, baseContentType, ownerUser);
    }

    protected ContentFile() {
        this.fileTransformations = new HashSet<>();
    }

    // Helpers
    private void populateBaseData(String name, String extension, String contentType, String baseContentType, UserCredential ownerUser) {
        this.baseContentType = baseContentType;
        this.owner = ownerUser;
        this.name = name;
        this.contentType = contentType;
        this.extension = extension;
    }
}
