package models.content;

import models.base.AbstractEntity;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.Set;

@NodeEntity
public class ContentPage extends ContentBase {

    public String route;
    public String name;
    public String title;
    public String preamble;
    public String mainBody;

    protected ContentPage() {
    }

    public ContentPage(String name) {
        this.route = cleanRoute(name);
        this.name = name;
    }

    public ContentPage(String route, String name) {
        this.route = cleanRoute(route);
        this.name = name;
    }

    private String cleanRoute(String input) {
        return input.toLowerCase().replace("/", "").replace("\\","").replaceAll("\\W+", "-");
    }
}
