package models.rating;

import models.UserCredential;
import models.base.AbstractEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.neo4j.annotation.*;

@RelationshipEntity
public class RatingUserCredential extends AbstractEntity {

    public Double rating;
    public String ratingIP;

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    @StartNode
    private UserCredential userWhoIsRating;

    @EndNode
    private UserCredential userRates;


}
