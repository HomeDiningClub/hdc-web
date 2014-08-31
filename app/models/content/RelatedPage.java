package models.content;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import org.springframework.data.neo4j.annotation.EndNode;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.RelationshipEntity;
import org.springframework.data.neo4j.annotation.StartNode;


@RelationshipEntity(type = "RELATED_PAGE")
public class RelatedPage extends AbstractEntity {

    @Fetch
    @StartNode
    public ContentPage relatedFrom;

    @Fetch
    @EndNode
    public ContentPage relatedTo;
    public Integer sortOrder;

    public RelatedPage(ContentPage relatedFrom, ContentPage relatedTo) {
        this.relatedFrom = relatedFrom;
        this.relatedTo = relatedTo;
        this.sortOrder = 100;
    }

    public RelatedPage(ContentPage relatedFrom, ContentPage relatedTo, Integer sortOrder) {
        this.relatedFrom = relatedFrom;
        this.relatedTo = relatedTo;
        this.sortOrder = sortOrder;
    }

    public RelatedPage() {

    }
}
