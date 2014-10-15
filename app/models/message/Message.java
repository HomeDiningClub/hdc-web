package models.message;

import models.UserCredential;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import services.InstancedServices;

import java.util.Date;

/**
 * Created by Tommy on 15/10/2014.
 */
@NodeEntity
public class Message extends  BaseMessage {

    @RelatedTo(type = RelationshipTypesJava.MESSAGE.Constant, direction = Direction.INCOMING)
    public UserCredential owner;

    @RelatedTo(type = RelationshipTypesJava.RESPONSE.Constant, direction = Direction.INCOMING)
    public Message response;

    public Message createMessage(Date date, Date time, int numberOfGuests, String request, UserCredential owner, String replyTo) {
        this.date = date;
        this.time = time;
        this.numberOfGuests = numberOfGuests;
        this.request = request;
        this.owner = owner;
        this.replyTo = replyTo;
        return this;
    }

    public UserCredential getOwner() {
        return InstancedServices.userCredentialService().fetchUserCredential(owner);
    }
}
