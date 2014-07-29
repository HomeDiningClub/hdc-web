package models.content;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelatedTo;
import java.util.HashSet;
import java.util.Set;

public abstract class ContentBase extends AbstractEntity {

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.CONTENT_STATE.Constant, direction = Direction.OUTGOING)
    public Set<ContentState> contentState;

    protected ContentBase() {
        this.contentState = new HashSet<>();
    }
}
