package models;

import java.util.HashSet;
import java.util.Set;

import models.base.AbstractEntity;
import models.modelconstants.RelationshipTypesJava;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.data.neo4j.support.index.IndexType;

@NodeEntity
public class World extends AbstractEntity {

    @Indexed
    public String name;

    @Indexed(indexType = IndexType.LABEL)
    public int moons;

    public String spokenLanguage;

    @Fetch
    @RelatedTo(type = RelationshipTypesJava.REACHABLE_BY_ROCKET.Constant, direction = Direction.OUTGOING)
    public Set<World> reachableByRocket;

    // Constructors
    public World(String name, int moons, String spokenLanguage) {
        this.reachableByRocket = new HashSet<>();
        this.name = name;
        this.moons = moons;
        this.spokenLanguage = spokenLanguage;
    }

    public World(String name, int moons) {
        this.reachableByRocket = new HashSet<>();
        this.name = name;
        this.moons = moons;
    }

    private World() {
        this.reachableByRocket = new HashSet<>();
    }
}