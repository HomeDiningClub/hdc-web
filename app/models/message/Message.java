package models.message;

import models.content.ContentBase;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class Message extends  ContentBase {

    public String fromProfileObjectId   = "";
    public String toProfileObjectId     = "";
    public String subject               = "";
    public String message               = "";

}
