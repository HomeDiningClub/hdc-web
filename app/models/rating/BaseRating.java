package models.rating;

import models.base.AuditEntity;

public abstract class BaseRating extends AuditEntity {

    public int ratingValue;
    public String userRaterIP;
    public String ratingComment;

}
