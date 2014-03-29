package models.vertices;

import com.tinkerpop.frames.Property;

public interface Profil extends VertexBase {

    @Property("fornamn")
    public String getFornamn();

    @Property("name")
    public void setFornamn(String fornamn);

    @Property("efternamn")
    public String getEfternamn();

    @Property("efernamn")
    public void setEfternamn(String efternamn);

}