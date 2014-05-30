package models.files;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class VideoFile extends ContentFile{

    @Transient
    private String bucketStoreDir = "videos/";

    public VideoFile(String name, String extension, String contentType){
        this.name = name;
        this.bucketDir = bucketStoreDir;
        this.contentType = contentType;
        this.extension = extension;
    }

    public VideoFile(){
        this.bucketDir = bucketStoreDir;
    }
}
