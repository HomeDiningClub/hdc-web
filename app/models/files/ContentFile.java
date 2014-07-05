package models.files;

import models.UserCredential;
import models.base.AbstractEntity;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import models.constants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ContentFile extends AbstractEntity {

    @Indexed
    public String name;
    public String extension;
    @Indexed
    public UUID key;
    public String contentType;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.OWNER.Constant, direction = Direction.OUTGOING)
    public UserCredential owner;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.OUTGOING)
    public Set<FileTransformation> fileTransformations = new HashSet<FileTransformation>();

    @Transient
    public String url;
    @Transient
    public String basePath;

    protected ContentFile() {
        this.key = UUID.randomUUID();
    }
}
