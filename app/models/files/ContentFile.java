package models.files;

import models.UserProfileData;
import models.base.AbstractEntity;

import java.util.Set;
import java.util.UUID;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ContentFile extends AbstractEntity {

    @Transient
    private String bucketStoreDir = "generic/";
    public String name;
    public String extension;
    public UUID key;
    public String bucketDir;
    public String contentType;

    @Fetch
    @RelatedTo(type = "OWNED_BY", direction = Direction.OUTGOING) // TODO - Improve enum
    public Set<UserProfileData> OwnedBy;

    @Transient
    public String url = bucketDir + key;

    protected ContentFile() {
        this.key = UUID.randomUUID();
        this.bucketDir = bucketStoreDir;
    }
}
