package models.content;

import enums.ContentStateEnums;
import models.base.AuditEntity;

public abstract class ContentBase extends AuditEntity {

    public String contentState;

    public void publish() {
        this.contentState = ContentStateEnums.PUBLISHED().toString();
    }

    public void unPublish() {
        this.contentState = ContentStateEnums.UNPUBLISHED().toString();
    }

    public boolean isPublished() {
        return contentState.equalsIgnoreCase(ContentStateEnums.PUBLISHED().toString());
    }

    protected ContentBase() {
        unPublish();
    }
}
