package models.content;

import models.base.AbstractEntity;
import models.enums.ContentStateEnums;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ContentState extends AbstractEntity {

    public String state;

    protected ContentState() {
        this.state = ContentStateEnums.PUBLISHED.name();
    }

    public ContentState(ContentStateEnums currentState) {
        this.state = currentState.name();
    }
}
