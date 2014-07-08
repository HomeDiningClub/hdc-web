package models;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

public class UserProfile extends AbstractEntity {


    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_CREDENTIAL.Constant, direction = Direction.BOTH)
    public UserCredential credential;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_LOCATION.Constant, direction = Direction.OUTGOING)
    public UserCredential locations;

    public String group = "";

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "userprofile_userid")
    public String userId = "";

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "userprofile_providerid")
    public String providerId = "";


    // Platinum premim
    // Gold
    // Silver
    // Admin
    public Integer memberStatus = 0;

    public boolean isHost = false;

    public String aboutMe = "";

    public String profilePicture = "";

    public String backgroundImage = "";

    // food pictures

}
