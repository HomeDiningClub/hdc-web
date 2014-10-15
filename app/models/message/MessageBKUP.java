package models.message;

import models.content.ContentBase;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class MessageBKUP extends ContentBase {

    public String fromProfileObjectId   = "";
    public String toProfileObjectId     = "";
    public String subject               = "";
    public String message               = "";

}
