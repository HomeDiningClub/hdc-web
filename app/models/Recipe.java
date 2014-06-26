package models;

import models.base.AbstractEntity;
import models.content.ContentBase;
import models.files.ImageFile;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.List;

@NodeEntity
public class Recipe extends ContentBase {

    @Indexed(indexType = IndexType.LABEL)
    public String name;
    public String mainBody;

    // Size: 460*305
    // Size: 151*100
    public ImageFile mainImage;

    public List<ImageFile> recipeImages;
    // Sizes:
    // 460*305
    // 151*100

    public Recipe(String name){
        this.name = name;
    }

    public Recipe(String name, String mainBody){
        this.name = name;
        this.mainBody = mainBody;
    }
}
