package models.content;

import org.springframework.data.annotation.Transient;

public class MenuItem {

    @Transient
    public String name;

    @Transient
    public String alt;

    @Transient
    public String title;

    @Transient
    public String url;

    @Transient
    public String cssClass;

    public MenuItem(String name, String alt, String title, String url, String cssClass){
        this.name = name;
        this.alt = alt;
        this.title = title;
        this.url = url;
        this.cssClass = cssClass;
    }

    public MenuItem(String name, String url){
        this.name = name;
        this.url = url;
        this.alt = "";
        this.title = "";
        this.cssClass = "";
    }

    public MenuItem(){
    }

}
