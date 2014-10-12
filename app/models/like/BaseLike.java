package models.like;

import models.base.AuditEntity;

public abstract class BaseLike extends AuditEntity {

    public boolean likes;
    public String userLikesIP;

}
