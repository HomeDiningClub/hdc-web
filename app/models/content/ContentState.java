package models.content;

import enums.ContentStateEnums;
import models.base.AbstractEntity;
//import org.springframework.data.neo4j.annotation.NodeEntity;

//@NodeEntity
public class ContentState {//extends AbstractEntity {

    public String state;

    protected ContentState() { this.state = ContentStateEnums.PUBLISHED().toString(); }

    public ContentState(ContentStateEnums currentState) {
        this.state = currentState.toString();
    }
}
