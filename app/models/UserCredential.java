package models;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import models.rating.RatingUserCredential;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import org.springframework.data.neo4j.template.Neo4jOperations;

import java.util.Set;


/**
 * This class stores credentials for accessing the application
 * provided by
 * 1. username/password
 * 2. Facebook integration
 */


@NodeEntity
public class UserCredential extends AbstractEntity {

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

    // Rating embryo,
    // Move to services

    @RelatedToVia(type = RelationshipTypesJava.RATED.Constant)
    @Fetch
    Set<RatingUserCredential> ratings;

    public RatingUserCredential rate(Neo4jOperations template, UserCredential userCredential, int ratingValue, String userIP, String comment) {
        final RatingUserCredential rating = template.createRelationshipBetween(this, userCredential, RatingUserCredential.class, RelationshipTypesJava.RATED.Constant, false);
        rating.rate(ratingValue, comment, userIP);
        return template.save(rating);
    }

    public int getAverageRating() {
        int sumOfRatingValues = 0, count = 0;
        for (RatingUserCredential rating : this.ratings) {
            sumOfRatingValues += rating.ratingValue;
            count++;
        }
        return count == 0 ? 0 : sumOfRatingValues / count;
    }

    public UserCredential() {

    }

    public UserCredential(String email, String providerId) {

        this.providerId     = providerId;
        this.emailAddress   = email;
    }



}
