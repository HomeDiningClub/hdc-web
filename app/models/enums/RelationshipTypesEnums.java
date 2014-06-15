package models.enums;

import org.neo4j.graphdb.RelationshipType;

public enum RelationshipTypesEnums implements RelationshipType {
    REACHABLE_BY_ROCKET,
    OWNED_BY,
    CONTENT_STATE
}