package models.event;

import models.content.ContentBase;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class MealType extends ContentBase {

    public String name;
    public int order;

    public MealType(String name, Integer order){
        this.name = name;
        this.order = order;
    }
    public MealType(String name){
        this.name = name;
        this.order = 0;
    }
    private MealType(){}
}
