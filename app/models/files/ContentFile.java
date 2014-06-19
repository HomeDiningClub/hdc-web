package models.files;

import models.UserCredential;
import models.base.AbstractEntity;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.UUID;

import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ContentFile extends AbstractEntity {

    @Transient
    private String bucketStoreDir = "generic/";
    @Indexed
    public String name;
    public String extension;
    @Indexed
    public UUID key;
    public String bucketDir;
    public String contentType;

    @Fetch
    @RelatedTo(type = "OWNED_BY", direction = Direction.OUTGOING) // TODO - Improve enum
    public Set<UserCredential> OwnedBy;

    @Transient
    public String url;
    //public String getUrl() throws MalformedURLException {
    //    return new URL("https://s3.amazonaws.com/" + this.bucketDir + this.key).toString();
    //}

    protected ContentFile() {
        this.key = UUID.randomUUID();
        this.bucketDir = bucketStoreDir;
        //this.url = this.bucketDir + this.key;
    }
}
