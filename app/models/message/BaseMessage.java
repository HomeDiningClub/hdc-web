package models.message;

import models.base.AuditEntity;
import models.modelconstants.RelationshipTypesJava;

import java.util.Date;

public abstract class BaseMessage extends AuditEntity {

    public Date date;
    public Date time;
    public int numberOfGuests;
    public String phone;
    public String request;
    public String sender;
    public String receiver;
    public String type;
    public Boolean read;
}
