package models.files;

import models.UserCredential;
import models.base.AbstractEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

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
public abstract class ContentFile extends AbstractEntity {

    @Indexed
    public UUID contentFileId;

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    @Indexed
    public String name;
    public String extension;
    public String contentType;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.OWNER.Constant, direction = Direction.BOTH)
    public UserCredential owner;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.OUTGOING)
    public Set<FileTransformation> fileTransformations = new HashSet<FileTransformation>();


    public FileTransformation getTransformByName(String name)
    {
        for (FileTransformation transform : this.fileTransformations) {
            if(transform.name.equalsIgnoreCase(name))
                return transform;
        }
        return null;
    }


    @Transient
    public String url;
    @Transient
    public String basePath;

    protected ContentFile() {
        this.contentFileId = UUID.randomUUID();
    }
}
