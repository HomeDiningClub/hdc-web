package models;

import interfaces.IEditable;
import models.base.AuditEntity;
import models.files.ContentFile;
import models.modelconstants.RelationshipTypesJava;
import models.modelconstants.UserLevelJava;
import models.profile.TagWord;
import models.location.County;
import models.profile.TaggedFavoritesToUserProfile;
import models.profile.TaggedLocationUserProfile;
import models.profile.TaggedUserProfile;
import org.neo4j.graphdb.Direction;
import org.springframework.data.annotation.Transient;
import org.springframework.data.neo4j.annotation.*;
import org.springframework.data.neo4j.support.index.IndexType;
import scala.Int;
import services.InstancedServices;

import java.text.ParseException;
import java.util.*;
import java.lang.Boolean;
import java.text.SimpleDateFormat;
import java.util.Calendar;

@NodeEntity
public class ViewedByMember extends AuditEntity implements IEditable {

    private Set<String> userAccessLog;
    private int numberOfViews = 0;


    public void addNewViews(int numberOfViews){
        this.numberOfViews = this.numberOfViews + numberOfViews;
    }

    public void viewedBy(String name, String date) {
        initUserAccessLog();
        String objectValue = createLogPost(name, date);
        userAccessLog.add(objectValue);
    }

    public boolean isViewedByNameOnDate(String name, String date) {
        initUserAccessLog();
        String objectValue = createLogPost(name, date);
        return userAccessLog.contains(objectValue);
    }

    public void remove(String name, String date) {
        String str = createLogPost(name, date);
        remove(str);
    }

    public void remove(String str) {
        initUserAccessLog();
        userAccessLog.remove(str);
    }

    public int getSize() {
        initUserAccessLog();
        return this.userAccessLog.size();
    }

    public int getNumberOfViews() {
        return numberOfViews + getSize();
    }


    public Iterator<String> getIterator() {
        initUserAccessLog();
        return userAccessLog.iterator();
    }



    public void initUserAccessLog() {
        if(userAccessLog == null) {
            this.userAccessLog = new HashSet<>();
        }
    }





    @Transient
    public Boolean isEditableBy(UUID objectId){
        if(objectId != null)
            return true;
        else
            return false;
    }

    // data format utils


    public String createLogPost(String name, String date) {
        StringBuilder txt = new StringBuilder();
        txt.append(name);
        txt.append(",");
        txt.append(date);
        return txt.toString();
    }

    public String getNamne(String str) {
        int index = str.indexOf(",");
        return str.substring(0, index).trim();
    }


    public String getDateAsString(String str) {
        int index = str.indexOf(",");
        return str.substring(index+1).trim();
    }






}