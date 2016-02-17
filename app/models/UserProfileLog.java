package models;

import traits.IEditable;
import models.base.AuditEntity;
import org.springframework.data.annotation.Transient;

import java.util.UUID;


public class UserProfileLog extends AuditEntity implements IEditable {

      // counter
      // named user
      // anonymouse user
    Integer numberOfVists = 0;

    Integer anonymouseVisits = 0;
    Integer memberVisits = 0;



    //todo UserCredential=>UserProfile=>UserProfileLog
    @Transient
    public Boolean isEditableBy(UUID objectId){
        /*
        if(objectId != null && objectId.equals(this.getOwner().objectId))
            return true;
        else
            return false;
        */
        return false;
    }


}
