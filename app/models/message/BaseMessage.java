package models.message;

import models.base.AuditEntity;

import java.util.Date;

/**
 * Created by Tommy on 14/10/2014.
 */
public abstract class BaseMessage extends AuditEntity {

    public Date date;
    public Date time;
    public int numberOfGuests;
    public String request;
    public String replyTo = "";
}
