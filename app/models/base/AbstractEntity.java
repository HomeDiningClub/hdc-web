package models.base;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.GraphId;
import java.util.UUID;

public abstract class AbstractEntity {

    @GraphId
    public Long graphId; //java.lang.Long
    public UUID objectId;

    // This method is needed to compare two different objects from DB using hash
    // Otherwise they are different objects
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (graphId == null || obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return graphId.equals(((AbstractEntity) obj).graphId);
    }

    @Transient
    private Integer hash;

    @Override
    public int hashCode() {
        if (hash == null) hash = graphId == null ? System.identityHashCode(this) : graphId.hashCode();
        return hash.hashCode();
    }

    protected void setUniqueId() {
        if(this.objectId == null)
            this.objectId = UUID.randomUUID();
    }

    protected AbstractEntity(){
        setUniqueId();
    }

    public String getHashCodeAsString() {
        return String.valueOf(hashCode());
    }
}
