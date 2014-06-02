package models.files;

import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ImageFile extends ContentFile{

    @Transient
    private String bucketStoreDir = "images/";

    public ImageFile(String name, String extension, String contentType){
        this.name = name;
        this.bucketDir = bucketStoreDir;
        this.contentType = contentType;
        this.extension = extension;
    }

    public ImageFile(){
        this.bucketDir = bucketStoreDir;
        //this.url = this.bucketDir + this.key;
    }
}
