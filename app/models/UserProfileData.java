package models;

import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;
import securesocial.core.Identity;



public class UserProfileData extends AbstractEntity {

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "usernameProfileData")
    public String userName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "emailProfileData")
    public String emailAddress;

    public String passWord;

    public Identity identity;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "firstnameProfileData")
    public String firstName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "lastnameProfileData")
    public String lastName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "aboutmeProfileData")
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



    public UserProfileData() {
        this.firstName  = "";
        this.lastName   = "";
        this.aboutMe    = "";
        this.emailAddress = "";
        this.passWord = "";
        this.userName = "";
        //this.id = 0L;


    }


}
