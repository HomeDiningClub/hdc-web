package models.base;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
//import org.springframework.data.auditing.DateTimeProvider;
//import org.springframework.data.auditing.CurrentDateTimeProvider;
//import org.springframework.data.domain.AuditorAware;
//import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public abstract class AuditEntity extends AbstractEntity {

    /* Fields */
    @CreatedDate
    private Date createdDate;
    @CreatedBy
    private UUID createdBy;
    @LastModifiedDate
    private Date lastModifiedDate;
    @LastModifiedBy
    private UUID lastUpdatedBy;

    /* Getters & Setters */

    public void setCreatedDate(Date createdDate){
        this.createdDate = createdDate;
    }
    public Date getCreatedDate(){
        return this.createdDate;
    }
    public Date getLastModifiedDate(){
        return this.lastModifiedDate;
    }
    public void setLastModifiedDate(Date lastModifiedDate){
        this.lastModifiedDate = lastModifiedDate;
    }

    /**
     * @deprecated add your own user auditing, these are not implemented due to stateless event can't fetch request.user
     */
    @Deprecated
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
    @Deprecated
    public UUID getCreatedBy() {
        return this.createdBy;
    }
    @Deprecated
    public void setLastUpdatedBy(UUID lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }
    @Deprecated
    public UUID getLastUpdatedBy() {
        return this.lastUpdatedBy;
    }

}
