package models;

import interfaces.IEditable;
import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;
import models.rating.RatesRecipe;
import models.rating.RatesUserCredential;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import play.libs.Scala;
import scala.Option;
import securesocial.core.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.lang.Boolean;


/**
 * This class stores credentials for accessing the application
 * provided by
 * 1. username/password
 * 2. Facebook integration
 */


@NodeEntity
public class UserCredential extends AuditEntity implements Identity, IEditable {

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

    public String personNummer              = "";

    // passwordInfo

    public String hasher = "";

    public String password = "";

    public String salt = "";

    // Authorization
    @Fetch
    @RelatedTo(type = RelationshipTypesJava.IN_ROLE.Constant, direction = Direction.OUTGOING)
    public Set<UserRole> roles;

    // Profile information
    @Fetch
    @RelatedTo(type = "IN_PROFILE", direction = Direction.OUTGOING)
    public Set<UserProfile> profiles;

    // Rating
    @RelatedToVia(type = RelationshipTypesJava.RATED_USER.Constant, direction = Direction.INCOMING)
    private Set<RatesUserCredential> ratings;

    @RelatedToVia(type = RelationshipTypesJava.RATED_USER.Constant, direction = Direction.OUTGOING)
    private Set<RatesUserCredential> hasRatedUsers;

    @RelatedToVia(type = RelationshipTypesJava.RATED_RECIPE.Constant, direction = Direction.OUTGOING)
    private Set<RatesRecipe> hasRatedRecipes;

    // Verify the object owner
    @Transient
    public Boolean isEditableBy(UUID objectId){
        if(objectId != null && this.objectId != null && objectId.equals(this.objectId))
            return true;
        else
            return false;
    }

    // Rating - average
    public int getAverageRating() {
        int sumOfRatingValues = 0, count = 0;
        for (RatesUserCredential rating : this.getRatings()) {
            sumOfRatingValues += rating.ratingValue;
            count++;
        }
        return count == 0 ? 0 : sumOfRatingValues / count;
    }

    // Rating count
    public int getNrOfRatings() {
        int count = 0;

        if(this.getRatings() != null)
            count = this.getRatings().size();

        return count;
    }

    // Getter & setters
    public void addRating(RatesUserCredential ratingToAdd) {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        this.ratings.add(ratingToAdd);
    }

    @Fetch
    public Set<RatesUserCredential> getRatings() {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        return this.ratings;
    }

    @Fetch
    public Set<RatesUserCredential> getHasRatedUsers() {
        if(this.hasRatedUsers == null)
            this.hasRatedUsers = new HashSet<>();

        return this.hasRatedUsers;
    }

    @Fetch
    public Set<RatesRecipe> getHasRatedRecipes() {
        if(this.hasRatedRecipes == null)
            this.hasRatedRecipes = new HashSet<>();

        return this.hasRatedRecipes;
    }


    public UserCredential() {
        this.roles = new HashSet<>();
        this.ratings = new HashSet<>();
        this.profiles = new HashSet<>();
    }

    public UserCredential(String email, String providerId, Set<UserRole> roles) {
        this.roles          = roles;
        this.providerId     = providerId;
        this.emailAddress   = email;
    }

    public UserCredential(String email, String providerId, UserRole role) {
        HashSet<UserRole> rolesToAdd = new HashSet<>();
        rolesToAdd.add(role);

        this.roles          = rolesToAdd;
        this.providerId     = providerId;
        this.emailAddress   = email;
    }

    public UserCredential(String email, String providerId) {
        this.roles = new HashSet<>();
        this.ratings = new HashSet<>();
        this.providerId     = providerId;
        this.emailAddress   = email;
    }


    // implements Identity

    @Override
    public IdentityId identityId() {
        IdentityId identityId = new IdentityId(this.userId, this.providerId);
        return identityId;
    }

    @Override
    public String firstName() {
        return this.firstName;
    }

    @Override
    public String lastName() {
        return this.lastName;
    }

    @Override
    public String fullName() {
        return fullName;
    }

    @Override
    public Option<String> email() {
        return (Scala.Option(emailAddress));
    }

    @Override
    public Option<String> avatarUrl() {
        return (Scala.Option(avatarUrl));
    }

    @Override
    public AuthenticationMethod authMethod() {
        return new AuthenticationMethod(this.authMethod);
    }

    @Override
    public Option<OAuth1Info> oAuth1Info() {
        return Scala.Option(new OAuth1Info(this.oAuth1InfoToken, this.oAuth1InfoSecret));
    }

    @Override
    public Option<OAuth2Info> oAuth2Info() {
        OAuth2Info oAuth2Info = new OAuth2Info
                (
                        oAuth2InfoAccessToken,
                        Scala.Option(oAuth2InfoTokenType.toString()),
                        Scala.Option((Object)oAuth2InfoExpiresIn),
                        Scala.Option(oAuth2InfoRefreshToken.toString())
                );

        return Scala.Option(oAuth2Info);
    }

    @Override
    public Option<PasswordInfo> passwordInfo() {

       PasswordInfo passwordInfo = null;
       passwordInfo = new PasswordInfo(this.hasher, this.password, Scala.Option(this.salt));

        return Scala.Option(passwordInfo);
    }
}
