package models;

import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;
import models.relationships.RelationshipTypes;
import securesocial.core.Identity;


@NodeEntity
public class UserProfileData {
    @GraphId
    public Long id;


    @Indexed(indexType = IndexType.FULLTEXT, indexName = "username")
    public String userName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "email")
    public String emailAddress;

    public String passWord;

    public Identity identity;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "firstname")
    public String firstName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "lastname")
    public String lastName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "aboutme")
    public String aboutMe;

    // Constructors
    public UserProfileData(String userName, String emailAddress) {
        this.emailAddress = emailAddress;
        this.userName = userName;
    }

    public UserProfileData(String userName, String emailAddress, String passWord) {
        this.emailAddress = emailAddress;
        this.userName = userName;
        this.passWord = passWord;
    }



    private UserProfileData() {
    }


}
