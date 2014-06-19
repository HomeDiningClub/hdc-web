package models;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;
import securesocial.core.Identity;


@NodeEntity
public class UserProfileData {
    @GraphId
    public Long id;


    @Indexed(indexType = IndexType.FULLTEXT, indexName = "username-ProfileData")
    public String userName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "email-ProfileData")
    public String emailAddress;

    public String passWord;

    public Identity identity;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "firstname-ProfileData")
    public String firstName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "lastname-ProfileData")
    public String lastName;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "aboutme-ProfileData")
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
