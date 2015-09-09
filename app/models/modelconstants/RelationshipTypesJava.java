package models.modelconstants;

import org.neo4j.graphdb.RelationshipType;

public final class RelationshipTypesJava {

    public final static class REACHABLE_BY_ROCKET implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "REACHABLE_BY_ROCKET";
    }

    public final  static  class  RESPONSE implements  RelationshipType {
        public  String name() { return Constant; }
        public  static  final  String Constant = "RESPONSE";
    }

    public final  static  class  INCOMING_MESSAGE implements  RelationshipType {
        public  String name() { return Constant; }
        public  static  final  String Constant = "INCOMING_MESSAGE";
    }

    public final  static  class  OUTGOING_MESSAGE implements  RelationshipType {
        public  String name() { return Constant; }
        public  static  final  String Constant = "OUTGOING_MESSAGE";
    }

    public final  static  class REQUEST implements  RelationshipType {
        public  String name() { return Constant; }
        public  static  final  String Constant = "REQUEST";
    }

    public final  static  class REPLY implements  RelationshipType {
        public  String name() { return Constant; }
        public  static  final  String Constant = "REPLY";
    }

    public final static class AVATAR_IMAGE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "AVATAR_IMAGE";
    }

    public final static class LIKES_USER implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "LIKES_USER";
    }

    public final static class LIKES_RECIPE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "LIKES_RECIPE";
    }

    public final static class LIKES_EVENT implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "LIKES_EVENT";
    }

    public final static class MAIN_IMAGE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "MAIN_IMAGE";
    }

    public final static class RELATED_PAGE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "RELATED_PAGE";
    }

    public final static class IN_ROLE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "IN_ROLE";
    }

    public final static class OWNER implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "OWNER";
    }

    public final static class IMAGES implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "IMAGES";
    }

    public final static class RATED_USER implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "RATED_USER";
    }

    public final static class RATED_RECIPE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "RATED_RECIPE";
    }

    public final static class HAS_RECIPES implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "HAS_RECIPES";
    }

    public final static class MEAL_TYPE implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "MEAL_TYPE";
    }

    public final static class HOSTS_EVENTS implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "HOSTS_EVENTS";
    }

    public final static class HAS_BLOGPOSTS implements RelationshipType {
        public String name() { return Constant; }
        public static final String Constant = "HAS_BLOGPOSTS";
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
