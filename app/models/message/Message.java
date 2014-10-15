package models.message;

import models.UserCredential;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import services.InstancedServices;

import java.util.Date;

/**
 * Created by Tommy on 14/10/2014.
 */
@NodeEntity
public class Message extends BaseMessage {

    @RelatedTo(type = "REQUEST", direction = Direction.INCOMING)
    public UserCredential owner;

    @RelatedTo(type = "RESPONSE", direction = Direction.OUTGOING)
    public MessagesReply response;

    public Message createMessage(Date date, Date time, int numberOfGuests, String request, UserCredential owner) {
        this.date = date;
        this.time = time;
        this.numberOfGuests = numberOfGuests;
        this.request = request;
        this.owner = owner;
        return  this;
    }

    public UserCredential getOwner() {
        return InstancedServices.userCredentialService().fetchUserCredential(owner);
    }
}
