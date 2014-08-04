package models.content;

import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class ContentPage extends ContentBase {

    public String name;
    public String route;
    public String title;
    public String preamble;
    public String mainBody;

    public String[] contentCategories;
    public ContentPage parentPage;
    public Boolean visibleInMenus;

    protected ContentPage() {
    }

    public ContentPage(String name) {
        this.route = cleanRoute(name);
        this.name = name;
        this.visibleInMenus = true;
    }

    public ContentPage(String name, Boolean visible) {
        this.route = cleanRoute(name);
        this.name = name;
        this.visibleInMenus = visible;
    }

    public ContentPage(String name, String route, Boolean visible) {
        this.route = cleanRoute(route);
        this.name = name;
        this.visibleInMenus = visible;
    }

    public ContentPage(String name, String route, String title, ContentPage parentPage, Boolean visible) {
        this.route = cleanRoute(route);
        this.parentPage = parentPage;
        this.name = name;
        this.title = title;
        this.visibleInMenus = visible;
    }

    public ContentPage(String name, String route, String title, String preamble, ContentPage parentPage, Boolean visible) {
        this.route = cleanRoute(route);
        this.parentPage = parentPage;
        this.name = name;
        this.preamble = preamble;
        this.title = title;
        this.visibleInMenus = visible;
    }

    public ContentPage(String name, String route, String title, String preamble, String mainBody, ContentPage parentPage, Boolean visible) {
        this.route = cleanRoute(route);
        this.parentPage = parentPage;
        this.name = name;
        this.preamble = preamble;
        this.title = title;
        this.mainBody = mainBody;
        this.visibleInMenus = visible;
    }

    private String cleanRoute(String input) {
        return input.toLowerCase().replace("/", "").replace("\\","").replaceAll("\\W+", "-");
    }
}
