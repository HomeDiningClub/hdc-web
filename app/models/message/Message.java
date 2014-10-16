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

    @RelatedTo(type = RelationshipTypesJava.OUTGOING_MESSAGE.Constant, direction = Direction.INCOMING)
    public UserCredential owner;

    @RelatedTo(type = RelationshipTypesJava.RESPONSE.Constant, direction = Direction.BOTH)
    public Message response;

    @RelatedTo(type = RelationshipTypesJava.INCOMING_MESSAGE.Constant, direction = Direction.OUTGOING)
    public UserCredential recipient;

    public Message createMessage(Date date, Date time, int numberOfGuests, String request, UserCredential owner, UserCredential recipient, String sender, String receiver, String type, String phone) {
        this.date = date;
        this.time = time;
        this.numberOfGuests = numberOfGuests;
        this.request = request;
        this.owner = owner;
        this.recipient = recipient;
        this.sender = sender;
        this.receiver = receiver;
        this.type = type;
        this.phone = phone;
        return this;
    }

    public UserCredential getOwner() {
        return InstancedServices.userCredentialService().fetchUserCredential(owner);
    }
    public UserCredential getRecipient() { return InstancedServices.userCredentialService().fetchUserCredential(recipient); }
    public Message getResponse() { return InstancedServices.messageService().fetchMessage(response); }
}
