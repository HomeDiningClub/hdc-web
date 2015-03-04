package utils;

import models.ViewedByMember;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ViewedByMemberUtil {

    private String DATE_STRING = "yyyy-MM-dd";
    private SimpleDateFormat sipmpleDateFormatString = new SimpleDateFormat(DATE_STRING);


    /**
     * Convert string to date
     * @param str with format YYYY-MM-DD
     * @return Date
     */
    public Date getDate(String str) {
        Date date = Calendar.getInstance().getTime();

        try {
            date = sipmpleDateFormatString.parse(getDateAsString(str));
        } catch(ParseException ex) {

        }

        return date;
    }


    /***
     * Return date as String from formated String <name>,<date>
     * @param str <name>,<date>
     * @return <date>
     */
    public String getDateAsString(String str) {
        int index = str.indexOf(",");
        return str.substring(index+1).trim();
    }

    /**
     * Return name
     * @param str
     * @return
     */
    public String getNamne(String str) {
        int index = str.indexOf(",");
        return str.substring(0, index).trim();
    }



    public String getDateObjectAsString(Date date) {
        String str = "";

        try {
            str = sipmpleDateFormatString.format(date);
        } catch(Exception ex) {

        }
        return str;
    }


    public String getNowString() {
        return getDateObjectAsString(Calendar.getInstance().getTime());
    }

    public Date xDayEarlier(int daysEarliers) {
        daysEarliers =  -1 * daysEarliers;
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, daysEarliers);
      return cal.getTime();
    }


    public void removeAllAccessOlderThen(Date datum, ViewedByMember view) {

        view.initUserAccessLog();
        Iterator<String> itter = view.getIterator();
        Set<String> toRemove = new HashSet<String>();
        int noOfRemovedViews = 0;

        while(itter.hasNext()) {
            String str = itter.next();
            Date pos = getDate(str);

            if(pos.before(datum)) {
                toRemove.add(str);
            }
        }

        for(String txt: toRemove) {
            view.remove(txt);
            noOfRemovedViews++;
        }

        view.addNewViews(noOfRemovedViews);
    }




    public Iterator<String> getIterator(ViewedByMember view) {
        view.initUserAccessLog();
        return view.getIterator();
    }

    public Iterator<String> getNameIteratorByDate(ViewedByMember view, String date) {
        ArrayList<String> arr = new ArrayList<String>();

        Iterator<String> iter = getIterator(view);

        while(iter.hasNext()) {
            String log = iter.next();
            String nDate = getDateAsString(log);
            if(nDate.equals(date)) {
                arr.add(getNamne(log));
            }
        }

        return arr.iterator();
    }



    public Iterator<String> getNameIterator(ViewedByMember view) {
        ArrayList<String> arr = new ArrayList<String>();
        HashSet<String> hSet = new HashSet<String>();

        Iterator<String> iter = getIterator(view);

        while(iter.hasNext()) {
            String log = iter.next();
            String nDate = getDateAsString(log);
            String nName = getNamne(log);
            hSet.add(nName);
        }

        return hSet.iterator();
    }


}



