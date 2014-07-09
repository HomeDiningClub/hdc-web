package models.profile;


import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class TagWord  extends AbstractEntity {

    @Indexed
    public String tagName      = "";
    public String tagId        = "";
    public String orderId      = "";

    @Indexed
    public String tagGroupName = "";


    public TagWord() {

    }

    public TagWord(String tagName, String tagId, String orderId) {

        this.tagName        = tagName;
        this.tagId          = tagId;
        this.orderId        = orderId;
    }

    public TagWord(String tagName, String tagId, String orderId, String tagGroupName) {

        this.tagName        = tagName;
        this.tagId          = tagId;
        this.orderId        = orderId;
        this.tagGroupName   = tagGroupName;
    }
}
