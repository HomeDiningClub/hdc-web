package models.content;

import models.UserProfileData;
import models.base.AbstractEntity;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

public abstract class ContentBase extends AbstractEntity {

    @CreatedDate
    public Date createdDate;

    @LastModifiedDate
    public Date lastModifiedDate;

    @Fetch
    @RelatedTo(type = "CONTENT_STATE", direction = Direction.OUTGOING) // TODO - Improve enum (http://components.neo4j.org/neo4j/1.6.M02/apidocs/org/neo4j/graphdb/DynamicRelationshipType.html)
    public Set<ContentState> contentState;

}
