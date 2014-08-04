package models.content;

import enums.ContentStateEnums;
import models.base.AbstractEntity;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

public abstract class ContentBase extends AbstractEntity {

    @CreatedDate
    public Long createdDate;

    @LastModifiedDate
    public Long lastModifiedDate;

    public String contentState;

    public void publish() {
        this.contentState = ContentStateEnums.PUBLISHED().toString();
    }

    public void unPublish() {
        this.contentState = ContentStateEnums.UNPUBLISHED().toString();
    }

    protected ContentBase() {
        unPublish();
    }
}
