package models;

import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;


@NodeEntity
public class UserRole extends AbstractEntity {

    @Indexed(unique = true)
    public String name;

    public UserRole(String name){
        this.name = name;
    }

    protected UserRole(){ }
}
