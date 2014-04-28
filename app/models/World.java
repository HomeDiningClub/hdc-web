package models;

import java.util.Set;

import models.base.AbstractEntity;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;
import models.relationships.RelationshipTypes;

@NodeEntity
public class World extends AbstractEntity {

    //@GraphId
    //public Long id;

    @Indexed
    public String name;

    @Indexed(indexType = IndexType.LABEL)
    public int moons;

    public String spokenLanguage;

    @Fetch
    @RelatedTo(type = "REACHABLE_BY_ROCKET", direction = Direction.OUTGOING) // TODO - Improve enum
    public Set<World> reachableByRocket;

    // Constructors
    public World(String name, int moons, String spokenLanguage) {
        this.name = name;
        this.moons = moons;
        this.spokenLanguage = spokenLanguage;
    }

    public World(String name, int moons) {
        this.name = name;
        this.moons = moons;
    }

    private World() {
    }
}