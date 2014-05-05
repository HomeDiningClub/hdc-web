package models.files;

import models.base.AbstractEntity;
import java.util.UUID;
import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
@TypeAlias("ContentFile")
public class ContentFile extends AbstractEntity {

    @Transient
    private String bucketStoreDir = "generic/";

    //@Indexed(unique = false)
    public String name;

    //@Indexed(unique = true)
    public UUID key;

    public String url;
    public String bucketDir;

    protected ContentFile() {
        this.key = UUID.randomUUID();
        this.bucketDir = bucketStoreDir;
    }
}
