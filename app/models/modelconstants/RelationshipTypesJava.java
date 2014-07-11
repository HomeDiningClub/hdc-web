package models.modelconstants;

import org.neo4j.graphdb.RelationshipType;

public final class RelationshipTypesJava {

    public final static class REACHABLE_BY_ROCKET implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "REACHABLE_BY_ROCKET";
    }

    public final static class OWNER implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "OWNER";
    }

    public final static class USED_BY implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "USED_BY";
    }

    public final static class RATED implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "RATED";
    }

    public static class RECOMMENDED implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "RECOMMENDED";
    }

    public static class FILE_TRANSFORMATION implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "FILE_TRANSFORMATION";
    }

    public static class CONTENT_STATE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "CONTENT_STATE";
    }

    public static class PROFILE_CREDENTIAL implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "PROFILE_CREDENTIAL";
    }

    public static class PROFILE_LOCATION implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "PROFILE_LOCATION";
    }

}
