package models.event;

import models.content.ContentBase;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class AlcoholServing extends ContentBase {

    public String name;
    public int order;

    public AlcoholServing(String name, Integer order){
        this.name = name;
        this.order = order;
    }
    public AlcoholServing(String name){
        this.name = name;
        this.order = 0;
    }
    private AlcoholServing(){}
}
