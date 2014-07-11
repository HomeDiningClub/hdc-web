package models;

import models.content.ContentBase;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Recipe extends ContentBase {

    @Indexed(indexType = IndexType.FULLTEXT,indexName = "recipeName")
    public String name;
    public String mainBody;

    @Fetch
    public ContentFile mainImage;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.USED_BY.Constant, direction = Direction.OUTGOING)
    public Set<ContentFile> recipeImages = new HashSet<>();

    public Recipe(String name, String mainBody, ContentFile mainImage, Set<ContentFile> recipeImages){
        this.mainImage = mainImage;
        this.recipeImages = recipeImages;
        this.name = name;
        this.mainBody = mainBody;
    }

    public Recipe(String name, String mainBody){
        this.name = name;
        this.mainBody = mainBody;
    }

    public Recipe(String name){
        this.name = name;
    }

    protected Recipe(){ }
}
