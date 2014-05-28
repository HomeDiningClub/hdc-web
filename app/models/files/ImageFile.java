package models.files;

import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ImageFile extends ContentFile{

    @Transient
    private String bucketStoreDir = "images/";

    public ImageFile(String name){
        this.name = name;
        this.bucketDir = bucketStoreDir;
    }

    public ImageFile(){
        this.bucketDir = bucketStoreDir;
    }
}
