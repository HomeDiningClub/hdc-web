package models.content;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedToVia;
import customUtils.Helpers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@NodeEntity
public class ContentPage extends ContentBase {

    // Definitions
    public String name;
    public String route;
    public String title;
    public String preamble;
    public String mainBody;

    public String[] contentCategories;
    public Boolean visibleInMenus;

    @RelatedToVia(type = "RELATED_PAGE", direction = Direction.BOTH)
    private Set<RelatedPage> relatedPages;

//    @RelatedTo(type = "IS_CHILD_OF", direction = Direction.OUTGOING)
//    public Set<ContentPage> isParentOf;
//
//    @RelatedTo(type = "IS_CHILD_OF", direction = Direction.INCOMING)
//    public ContentPage isChildOf;

    // Getter & setters
    @Fetch
    public Set<RelatedPage> getRelatedPages(){
        return this.relatedPages;
    }

    public RelatedPage addRelatedPageSorted(ContentPage contentPage, Integer sortOrder) {
        if(this.relatedPages == null)
            this.relatedPages = new HashSet<>();

        RelatedPage relatedPage = new RelatedPage(this, contentPage, sortOrder);
        relatedPages.add(relatedPage);
        return relatedPage;
    }
    public RelatedPage addRelatedPage(ContentPage contentPage) {
        if(this.relatedPages == null)
            this.relatedPages = new HashSet<>();

        RelatedPage relatedPage = new RelatedPage(this, contentPage);
        relatedPages.add(relatedPage);
        return relatedPage;
    }

    public void removeAllRelatedPages() {
        Iterable<RelatedPage> relPages = this.relatedPages;

        if(relPages != null) {
            // Temporary store all
            ArrayList<RelatedPage> arr = new ArrayList<RelatedPage>();
            for (RelatedPage tagProfile: relPages) {
                arr.add(tagProfile);
            }

            // Remove all
            Iterator<RelatedPage> itter2 = arr.iterator();
            while (itter2.hasNext()) {
                this.relatedPages.remove(itter2.next());
            }
        }
    }

    public void removeRelatedPage(RelatedPage relatedPage) {
        if(this.relatedPages != null)
            this.relatedPages.remove(relatedPage);
    }


    // Helpers
    private String cleanRoute(String input) {
        return Helpers.createRoute(input);
        //return input.toLowerCase().replace("/", "").replace("\\","").replaceAll("\\W+", "-");
    }


    // Constructors
    protected ContentPage() {
    }

    public ContentPage(String name) {
        this.route = cleanRoute(name);
        this.name = name;
        this.visibleInMenus = true;
        this.relatedPages = new HashSet<>();
    }

    public ContentPage(String name, Boolean visible) {
        this.route = cleanRoute(name);
        this.name = name;
        this.visibleInMenus = visible;
        this.relatedPages = new HashSet<>();
    }

    public ContentPage(String name, String route, Boolean visible) {
        this.route = cleanRoute(route);
        this.name = name;
        this.visibleInMenus = visible;
        this.relatedPages = new HashSet<>();
    }

    public ContentPage(String name, String route, String title, ContentPage relatedPage, Boolean visible) {
        if(relatedPage != null)
            this.addRelatedPage(relatedPage);

        this.route = cleanRoute(route);
        this.name = name;
        this.title = title;
        this.visibleInMenus = visible;
    }


}
