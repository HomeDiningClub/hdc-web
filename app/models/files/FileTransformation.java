package models.files;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import services.InstancedServices;

@NodeEntity
public class FileTransformation extends AbstractEntity {

    @Indexed
    public String name;
    public int width;
    public int height;
    public double scale;
    public String transformationType;
    public String extension;

    @RelatedTo(type = RelationshipTypesJava.FILE_TRANSFORMATION.Constant, direction = Direction.INCOMING)
    private ContentFile ownerFile;

    @Fetch
    public ContentFile getOwnerFile(){
        return this.ownerFile;
    }

    public String getBasePath(){
        if(this.ownerFile != null) {
            return this.ownerFile.getBasePath();
        }else{
            return null;
        }
    }

    public String getUrl(){
        return InstancedServices.contentFileService().getBucketUrl(this);
    }

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
