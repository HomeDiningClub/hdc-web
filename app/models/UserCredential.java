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


/**
 * This class stores credentials for accessing the application
 * provided by
 * 1. username/password
 * 2. Facebook integration
 */


@NodeEntity
public class UserCredential {

    @GraphId
    public Long id;

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "userId")
    public String userId = "";

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "emailAddress")
    public String emailAddress = "";

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "providerId")
    public String providerId = "";


    @Indexed(indexType = IndexType.FULLTEXT, indexName = "firstName")
    public String firstName = "";

    @Indexed(indexType = IndexType.FULLTEXT, indexName = "lastName")
    public String lastName = "";

    public String fullName = "";

    public String avatarUrl = "";

    public String authMethod = "";

    // oAuth1Info

    public String oAuth1InfoToken           = "";

    public String oAuth1InfoSecret          = "";

    // oAuth2Info

    public String oAuth2InfoAccessToken     = "";

    public String oAuth2InfoTokenType       = "";

    public String oAuth2InfoExpiresIn       = "";

    public String oAuth2InfoRefreshToken    = "";

    // passwordInfo

    public String hasher = "";

    public String password = "";

    public String salt = "";


    public UserCredential() {

    }

    public UserCredential(String email, String providerId) {

        this.providerId     = providerId;
        this.emailAddress   = email;
    }


}
