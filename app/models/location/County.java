package models.location;

import models.content.ContentBase;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class County extends ContentBase {

    public String name;
    public int order;

    public County(String name, Integer order){
        this.name = name;
        this.order = order;
    }
    public County(String name){
        this.name = name;
        this.order = 0;
    }
    private County(){}
}
