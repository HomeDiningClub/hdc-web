package models.relationships;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypes implements RelationshipType {
    REACHABLE_BY_ROCKET,
    OWNED_BY
}