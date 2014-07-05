package models;

import models.base.AbstractEntity;
import models.constants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.Set;

public class UserProfile extends AbstractEntity {


    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_CREDENTIAL.Constant, direction = Direction.BOTH)
    public UserCredential credential;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.PROFILE_LOCATION.Constant, direction = Direction.OUTGOING)
    public UserCredential locations;

    public String group = "";

    // Platinum premim
    // Gold
    // Silver
    // Admin

    public boolean isHost = false;

    public String aboutMe = "";

    public String profilePicture = "";

    public String backgroundImage = "";

    // food pictures

}
