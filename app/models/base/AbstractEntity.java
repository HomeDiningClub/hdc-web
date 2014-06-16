package models.base;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

public abstract class AbstractEntity {

    @GraphId
    public Long id; //java.lang.Long

    // This method is needed to compare two different objects from DB using hash
    // Otherwise they are different objects
    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (id == null || obj == null || !getClass().equals(obj.getClass())) {
            return false;
        }
        return id.equals(((AbstractEntity) obj).id);
    }

    @Transient
    private Integer hash;

    @Override
    public int hashCode() {
        if (hash == null) hash = id == null ? System.identityHashCode(this) : id.hashCode();
        return hash.hashCode();
    }

    public String getHashCodeAsString() {
        return String.valueOf(hashCode());
    }
}
