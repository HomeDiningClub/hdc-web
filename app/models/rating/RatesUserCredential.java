package models.rating;

import models.UserCredential;
import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.*;
import services.InstancedServices;

@RelationshipEntity(type = RelationshipTypesJava.RATED_USER.Constant)
public class RatesUserCredential extends BaseRating {

    @StartNode
    public UserCredential userWhoIsRating;

    @EndNode
    public UserCredential userRates;

    public UserCredential getUserWhoIsRating() {
        return InstancedServices.userCredentialService().fetchUserCredential(userWhoIsRating);
    }

    public UserCredential getUserRates() {
        return InstancedServices.userCredentialService().fetchUserCredential(userRates);
    }

    public RatesUserCredential rate(int ratingValue, String ratingComment, String userRaterIP) {
        this.userRaterIP = userRaterIP;
        this.ratingValue = ratingValue;
        this.ratingComment = ratingComment;
        return this;
    }

}
