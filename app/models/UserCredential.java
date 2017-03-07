package models;

import traits.IEditable;
import models.base.AuditEntity;
import models.like.UserCredentialLikeEvent;
import models.like.UserCredentialLikeRecipe;
import models.like.UserCredentialLikeUserCredential;
import models.modelconstants.RelationshipTypesJava;
import models.rating.RatesRecipe;
import models.rating.RatesUserCredential;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import play.libs.Scala;
import scala.Option;
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
public class UserCredential extends AuditEntity implements securesocial.core.GenericProfile, IEditable {

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

    // Likes
    @RelatedToVia(type = RelationshipTypesJava.LIKES_USER.Constant, direction = Direction.INCOMING)
    private Set<UserCredentialLikeUserCredential> likes;

    @RelatedToVia(type = RelationshipTypesJava.LIKES_USER.Constant, direction = Direction.OUTGOING)
    private Set<UserCredentialLikeUserCredential> hasLikedUsers;

    @RelatedToVia(type = RelationshipTypesJava.LIKES_RECIPE.Constant, direction = Direction.OUTGOING)
    private Set<UserCredentialLikeRecipe> hasLikedRecipes;

    @RelatedToVia(type = RelationshipTypesJava.LIKES_EVENT.Constant, direction = Direction.OUTGOING)
    private Set<UserCredentialLikeEvent> hasLikedEvents;

    public String getFullName() {
        String retString = "";
        if(firstName != null)
            retString = firstName;

        if(firstName != null && lastName != null)
            retString += " ";

        if(lastName != null)
            retString += lastName;

        return retString;
    }

    // Verify the object owner
    @Transient
    public Boolean isEditableBy(UUID objectId){
        return objectId != null && this.objectId != null && objectId.equals(this.objectId);
    }

    // Rating - average
/*    public int getAverageRating() {
        int sumOfRatingValues = 0, count = 0;
        for (RatesUserCredential rating : this.getRatings()) {
            sumOfRatingValues += rating.ratingValue;
            count++;
        }
        return count == 0 ? 0 : sumOfRatingValues / count;
    }*/

    // Rating count
/*    public int getNrOfRatings() {
        int count = 0;

        if(this.getRatings() != null)
            count = this.getRatings().size();

        return count;
    }*/

    // Getter & setters
    public void addRating(RatesUserCredential ratingToAdd) {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        this.ratings.add(ratingToAdd);
    }

    // Like
    public Set<UserCredentialLikeUserCredential> getLikes() {
        if(this.likes == null)
            this.likes = new HashSet<>();

        return this.likes;
    }

    public Set<UserCredentialLikeUserCredential> getHasLikedUsers() {
        if(this.hasLikedUsers == null)
            this.hasLikedUsers = new HashSet<>();

        return this.hasLikedUsers;
    }

    public Set<UserCredentialLikeRecipe> getHasLikedRecipes() {
        if(this.hasLikedRecipes == null)
            this.hasLikedRecipes = new HashSet<>();

        return this.hasLikedRecipes;
    }

    public Set<UserCredentialLikeEvent> getHasLikedEvents() {
        if(this.hasLikedEvents == null)
            this.hasLikedEvents = new HashSet<>();

        return this.hasLikedEvents;
    }

    // Like count
    public int getNrOfLikes() {
        int count = 0;

        if(this.getLikes() != null)
            count = this.getLikes().size();

        return count;
    }

    // Like add
    public void addLike(UserCredentialLikeUserCredential likeToAdd) {
        if(this.likes == null)
            this.likes = new HashSet<>();

        this.likes.add(likeToAdd);
    }

    public Set<RatesUserCredential> getRatings() {
        if(this.ratings == null)
            this.ratings = new HashSet<>();

        return this.ratings;
    }

    public Set<RatesUserCredential> getHasRatedUsers() {
        if(this.hasRatedUsers == null)
            this.hasRatedUsers = new HashSet<>();

        return this.hasRatedUsers;
    }

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

    public UserProfile getUserProfile() {
        return this.profiles.iterator().next();
    }

    public String getPhone() {

        if (profiles != null) {
            for(UserProfile profile : profiles) {
                if(profile.phoneNumber != null && !profile.phoneNumber.isEmpty())
                    return profile.phoneNumber;
            }
        }

        return  "";
    }

    // Implements securesocial.core.GenericProfile

    /*
    @Override
    public IdentityId identityId() {
        IdentityId identityId = new IdentityId(this.userId, this.providerId);
        return identityId;
    }*/

    @Override
    public String providerId() {
        return this.providerId;
    }

    @Override
    public String userId() {
        return this.userId;
    }

    @Deprecated
    /*
     * @deprecated
     * Don't use .firstName(), use .firstName instead
     */
    @Override
    public Option<String> firstName() {
        return Scala.Option(this.firstName);
    }

    @Deprecated
    /*
     * @deprecated
     * Don't use .lastName(), use .lastName instead
     */
    @Override
    public Option<String> lastName() {
        return Scala.Option(this.lastName);
    }

    @Deprecated
    /*
     * @deprecated
     * Don't use .fullName(), use .fullName instead
     */
    @Override
    public Option<String> fullName() {
        return Scala.Option(fullName);
    }

    @Deprecated
    /*
     * @deprecated
     * Don't use .email(), use .emailAddress instead
     */
    @Override
    public Option<String> email() {
        return (Scala.Option(emailAddress));
    }

    @Override
    public Option<String> avatarUrl() {
        return (Scala.Option(avatarUrl));
    }

    @Override
    public securesocial.core.AuthenticationMethod authMethod() {
        return new securesocial.core.AuthenticationMethod(this.authMethod);
    }

    @Override
    public Option<securesocial.core.OAuth1Info> oAuth1Info() {
        return Scala.Option(new securesocial.core.OAuth1Info(this.oAuth1InfoToken, this.oAuth1InfoSecret));
    }

    @Override
    public Option<securesocial.core.OAuth2Info> oAuth2Info() {
        securesocial.core.OAuth2Info oAuth2Info = new securesocial.core.OAuth2Info
                (
                        oAuth2InfoAccessToken,
                        Scala.Option(oAuth2InfoTokenType),
                        Scala.Option(oAuth2InfoExpiresIn),
                        Scala.Option(oAuth2InfoRefreshToken)
                );

        return Scala.Option(oAuth2Info);
    }

    @Override
    public Option<securesocial.core.PasswordInfo> passwordInfo() {
        securesocial.core.PasswordInfo passwordInfo = new securesocial.core.PasswordInfo(this.hasher, this.password, Scala.Option(this.salt));
        return Scala.Option(passwordInfo);
    }

}
