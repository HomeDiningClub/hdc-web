package models.base;

import org.springframework.data.annotation.Transient;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.support.index.IndexType;

import java.util.UUID;

public abstract class AbstractEntity {

    @GraphId
    public Long graphId; //java.lang.Long
    //@Indexed(indexType = IndexType.LABEL, level = Indexed.Level.INSTANCE)
    public UUID objectId;

    // This method is needed to compare two different objects from DB using hash
    // Otherwise they are different objects
    @Override
    public boolean equals(Object obj) {
        return this == obj || !(objectId == null || obj == null || !getClass().equals(obj.getClass())) && objectId.equals(((AbstractEntity) obj).objectId);
    }

    @Transient
    private Integer hash;

    @Override
    public int hashCode() {
        if (hash == null) hash = objectId == null ? System.identityHashCode(this) : objectId.hashCode();
        return hash.hashCode();
    }

    void setUniqueId() {
        if(this.objectId == null)
            this.objectId = UUID.randomUUID();
    }

    protected AbstractEntity(){
        setUniqueId();
    }

//    public String getHashCodeAsString() {
//        return String.valueOf(hashCode());
//    }
}
