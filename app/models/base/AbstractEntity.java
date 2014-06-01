package models.base;

import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public abstract class AbstractEntity {

    @GraphId
    public Long id;

    public Long getId() {
        return id;
    }

    /*
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
    */

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
