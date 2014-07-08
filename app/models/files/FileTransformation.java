package models.files;

import models.base.AbstractEntity;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class FileTransformation extends AbstractEntity {

    @Indexed(unique = false)
    public String name;
    public int width;
    public int height;
    public double scale;
    public String transformationType;
    public String extension;

    @Transient
    public String url;
    @Transient
    public String basePath;

    public FileTransformation(String name, double scale, String transformationType){
        this.name = name;
        this.scale = scale;
        this.transformationType = transformationType;
    }
    public FileTransformation(String name, int width, int height, String transformationType){
        this.name = name;
        this.width = width;
        this.height = height;
        this.transformationType = transformationType;
    }
    private FileTransformation(){    }
}
