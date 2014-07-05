package models.profile;


import models.base.AbstractEntity;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class TagWord  extends AbstractEntity {
    public String tagName = "";
    public String tagId   = "";
    public String orderId = "";


    public TagWord() {

    }

    public TagWord(String tagName, String tagId, String orderId) {

        this.tagName    = tagName;
        this.tagId      = tagId;
        this.orderId    = orderId;
    }
}
