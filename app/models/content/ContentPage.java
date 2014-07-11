package models.content;

import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class ContentPage extends ContentBase {

    @Indexed(indexType = IndexType.LABEL)
    public String name;
    @Indexed(indexType = IndexType.LABEL)
    public String route;
    public String title;
    public String preamble;
    public String mainBody;

    protected ContentPage() {
    }

    public ContentPage(String name) {
        this.route = cleanRoute(name);
        this.name = name;
    }

    public ContentPage(String name, String route) {
        this.route = cleanRoute(route);
        this.name = name;
    }

    public ContentPage(String name, String route, String title) {
        this.route = cleanRoute(route);
        this.name = name;
        this.title = title;
    }

    public ContentPage(String name, String route, String title, String preamble) {
        this.route = cleanRoute(route);
        this.name = name;
        this.preamble = preamble;
        this.title = title;
    }

    public ContentPage(String name, String route, String title, String preamble, String mainBody) {
        this.route = cleanRoute(route);
        this.name = name;
        this.preamble = preamble;
        this.title = title;
        this.mainBody = mainBody;
    }

    private String cleanRoute(String input) {
        return input.toLowerCase().replace("/", "").replace("\\","").replaceAll("\\W+", "-");
    }
}
