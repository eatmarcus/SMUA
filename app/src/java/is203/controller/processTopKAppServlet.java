package is203.controller;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import is203.dao.topKAppDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.NavigableMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Marcus
 */
@WebServlet(urlPatterns = {"/processTopKAppServlet"})
public class processTopKAppServlet extends HttpServlet {

    Date date1;
    Date date2;

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();

        String kValue = request.getParameter("numOfReports");
        String startDate = request.getParameter("startdate");
        String endDate = request.getParameter("enddate");
        String submit1 = request.getParameter("submit1");
        String submit2 = request.getParameter("submit2");
        String submit3 = request.getParameter("submit3");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int kValueInt = Integer.parseInt(kValue);
        Calendar dateToday = null;

        topKAppDAO tkaDAO = new topKAppDAO();

        try {
            date1 = sdf.parse(startDate);
            date2 = sdf.parse(endDate);
            //make end date + 1 day - 1 second
            Calendar c = Calendar.getInstance();
            c.setTime(date2);
            c.add(Calendar.DATE, 1);
            date2 = c.getTime();
            long t = date2.getTime();
            date2 = new Date(t - 1000);
            dateToday = Calendar.getInstance();

            if (date1.after(date2)) {
                String message = "Start Date cannot be after End Date.";
                request.setAttribute("errorForm", message);
                RequestDispatcher view = request.getRequestDispatcher("topKAppUsageForm.jsp");
                view.forward(request, response);
                return;
            } else if (date2.after(dateToday.getTime())) {
                date2 = dateToday.getTime();
            }

            if (submit1 != null && submit2 == null && submit3 == null) {
                String chosenFilter = request.getParameter("school");
                LinkedHashMap<String, Long> toReturn = processTopKGivenSchool(kValueInt, date1, date2, chosenFilter);
                if (toReturn == null) {
                    String msg = "No Results";
                    request.setAttribute("noResults", msg);
                    RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                    view.forward(request, response);
                    return;
                }
                request.setAttribute("resultSet", toReturn);
                RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                view.forward(request, response);
                return;
            } else if (submit1 == null && submit2 != null && submit3 == null) {
                //do Top-k students with most app usage (given an app category)            
                String chosenFilter = request.getParameter("cat1");
                LinkedHashMap<String, Long> toReturn = processStudentGivenCat(kValueInt, date1, date2, chosenFilter);
                if (toReturn == null) {
                    String msg = "No Results";
                    request.setAttribute("noResults", msg);
                    RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                    view.forward(request, response);
                    return;
                }
                request.setAttribute("processStudentResults", toReturn);
                RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                view.forward(request, response);
                return;
            } else if (submit1 == null & submit2 == null && submit3 != null) {
                //filter by category
                String chosenFilter = request.getParameter("cat2");
                LinkedHashMap<String, Long> toReturn = processSchoolGivenCat(kValueInt, date1, date2, chosenFilter);
                if (toReturn == null) {
                    String msg = "No Results";
                    request.setAttribute("noResults", msg);
                    RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                    view.forward(request, response);
                    return;
                }
                request.setAttribute("processSchoolResults", toReturn);
                RequestDispatcher view = request.getRequestDispatcher("topKAppResults.jsp");
                view.forward(request, response);
                return;
            }

        } catch (ParseException ex) {
            //if user dont fill start date or end date
            String message = "Please fill in both Start Date and End Date.";
            request.setAttribute("errorForm", message);
            RequestDispatcher view = request.getRequestDispatcher("topKAppUsageForm.jsp");
            view.forward(request, response);
            return;
        }

    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "process Top K App Servlet";
    }// </editor-fold>

    /**
     * Given a k value, school, start date and end date specified by user,
     * retrieve all relevant email, timestamp and app-names from SQL and process
     * the Top apps with most app usage time
     *
     * @param kValueInt number of top results wanted
     * @param date1 start date specified by user
     * @param date2 end date specified by user
     * @param chosenFilter school chosen by user
     * @return a LinkedHashMap with app name as key and app usage duration as
     * value
     * @throws IOException
     */
    public LinkedHashMap<String, Long> processTopKGivenSchool(int kValueInt, Date date1, Date date2, String chosenFilter) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        topKAppDAO tkDAO = new topKAppDAO();
        LinkedHashMap<String, ArrayList<String[]>> rs = (LinkedHashMap<String, ArrayList<String[]>>) tkDAO.getTopAppsWithFilterBySchool(date1, date2, chosenFilter);
        LinkedHashMap<String, Long> calculatedMap = new LinkedHashMap<String, Long>();
        Date dateTime1 = null;
        Date dateTime2 = null;
        Set<String> emailSet = rs.keySet();

        if (!emailSet.isEmpty()) {
            long timeDiffInSeconds = 0;
            Iterator<String> toIterateEmail = emailSet.iterator();

            while (toIterateEmail.hasNext()) {
                String currentEmail = toIterateEmail.next();
                ArrayList<String[]> currentList = rs.get(currentEmail);
                Iterator<String[]> toIterateList = currentList.iterator();
                String[] currentArr = toIterateList.next();
                String appName1 = currentArr[0];
                String timeStamp1 = currentArr[1];
                while (toIterateList.hasNext()) {

                    String[] nextArr = toIterateList.next();
                    String appName2 = nextArr[0];
                    String timeStamp2 = nextArr[1];
                    try {
                        dateTime1 = sdf.parse(timeStamp2);//earlier time 
                        dateTime2 = sdf.parse(timeStamp1);// later time
                        long timeDiff = dateTime1.getTime() - dateTime2.getTime();

                        timeDiffInSeconds = timeDiff / 1000;
                        if (timeDiffInSeconds > 120) {
                            timeDiffInSeconds = 10;
                        }
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    }
                    if (calculatedMap.get(appName1) != null) {

                        long currentDuration = calculatedMap.get(appName1);
                        currentDuration += timeDiffInSeconds;
                        calculatedMap.put(appName1, currentDuration);
                    } else {
                        long currentDuration = timeDiffInSeconds;
                        calculatedMap.put(appName1, currentDuration);
                    }
                    appName1 = appName2;
                    timeStamp1 = timeStamp2;
                    timeDiffInSeconds = 0;

                    if (!toIterateList.hasNext()) {

                        long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                        if (timeToAdd > 10) {
                            timeToAdd = 10;

                        }
                        if (calculatedMap.get(appName1) != null) {
                            long currentDuration = calculatedMap.get(appName1);
                            currentDuration += timeToAdd;
                            calculatedMap.put(appName1, currentDuration);
                        } else {
                            calculatedMap.put(appName1, timeToAdd);
                        }
                    }
                }

            }
            Set<String> appNameSet = calculatedMap.keySet();
            LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<String, Long>();
            Iterator<String> toIterAppName = appNameSet.iterator();
            long longestDuration = 0;
            int numberOfTimes = appNameSet.size();

            String appNameWithLongestDuration = "";
            for (int i = 0; i < numberOfTimes; i++) {

                toIterAppName = appNameSet.iterator();
                while (toIterAppName.hasNext()) {
                    String currentAppName = toIterAppName.next();
                    long currentDuration = calculatedMap.get(currentAppName);
                    if (currentDuration > longestDuration) {
                        longestDuration = currentDuration;
                        appNameWithLongestDuration = currentAppName;
                    }
                }
                sortedMap.put(appNameWithLongestDuration, longestDuration);
                calculatedMap.remove(appNameWithLongestDuration);

                longestDuration = 0;
                appNameWithLongestDuration = "";
            }

            LinkedHashMap<String, Long> toReturn = new LinkedHashMap<String, Long>();
            Set<String> keysForSortedMap = sortedMap.keySet();
            Iterator<String> toIterSortedMap = keysForSortedMap.iterator();
            String currentAppName = toIterSortedMap.next();
            long currentDuration = sortedMap.get(currentAppName);
            String finalAppName = currentAppName;
            int count = 1;
            int otherCounter = 0;
            while (toIterSortedMap.hasNext() && count < kValueInt) {
                String nextAppName = toIterSortedMap.next();
                long nextDuration = sortedMap.get(nextAppName);
                if (currentDuration == nextDuration) {
                    otherCounter++;
                    finalAppName = finalAppName + ", " + nextAppName;
                } else {
                    toReturn.put(finalAppName, currentDuration);
                    finalAppName = nextAppName;
                    currentDuration = nextDuration;
                    if (otherCounter < kValueInt) {
                        count++;

                        count += otherCounter;
                    }
                }
            }
            toReturn.put(finalAppName, currentDuration);
            return toReturn;
        }
        return null;
    }

    /**
     * Given a k value, category, start date and end date specified by user,
     * retrieve all relevant mac-address, name and timestamp from SQL and
     * process the Top students with most app usage time
     *
     * @param kValueInt number of results specified by user
     * @param date1 start date specified by user
     * @param date2 end date specified by user
     * @param chosenFilter category of app specified
     * @return a LinkedHashMap of mac-address and name as key and app usage time
     * as value
     * @throws IOException
     */
    public LinkedHashMap<String, Long> processStudentGivenCat(int kValueInt, Date date1, Date date2, String chosenFilter) throws IOException {
        //Value: duration, mac_address, name(String[])
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        topKAppDAO tkDAO = new topKAppDAO();
        LinkedHashMap<String, Long> calculatedMap = new LinkedHashMap<String, Long>();

        //string [] containing name & timestamp, app category as value
        LinkedHashMap<String, ArrayList<String[]>> rs = (LinkedHashMap<String, ArrayList<String[]>>) tkDAO.getTopKStudentsGivenCat(date1, date2, chosenFilter);
        Date dateTime1 = null;
        Date dateTime2 = null;
        long timeDiffInSeconds = 0;
        String[] calculatedArr = new String[3];
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //getting a set of macAddress from the results from DAO
        Set<String> macAddressList = rs.keySet();
        Iterator<String> toIterateMacAdd = macAddressList.iterator();
        while (toIterateMacAdd.hasNext()) {
            String currentMacAdd = toIterateMacAdd.next();
            ArrayList<String[]> ownerList = rs.get(currentMacAdd);
            if (ownerList.size() > 1) { // more than 1 timestamp entry
                String[] earlier = ownerList.get(0);
                String name1 = earlier[0];
                String timestamp1 = earlier[1];
                String appCat1 = earlier[2];
                for (int i = 1; i < ownerList.size(); i++) {
                    String[] later = ownerList.get(i);
                    String name2 = later[0];
                    String timestamp2 = later[1];
                    String appCat2 = later[2];
                    String key = currentMacAdd + ", " + name1;
                    if (appCat1.equalsIgnoreCase(chosenFilter)) { //appCat1 is the chosen filter
                        try {
                            dateTime1 = sdf.parse(timestamp2);//later time 
                            dateTime2 = sdf.parse(timestamp1);// earlier time
                            long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                            timeDiffInSeconds = timeDiff / 1000;
                            if (timeDiffInSeconds > 120) {
                                timeDiffInSeconds = 10;
                            }

                            if (calculatedMap.containsKey(key)) {
                                long currentDuration = calculatedMap.get(key);
                                currentDuration += timeDiffInSeconds;
                                calculatedMap.put(key, currentDuration);
                            } else {
                                long newDuration = timeDiffInSeconds;
                                calculatedMap.put(key, newDuration);
                            }
                        } catch (ParseException ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (appCat1.equalsIgnoreCase(chosenFilter) && (i == ownerList.size() - 1)) {
                        long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                        if (timeToAdd > 10) {
                            timeToAdd = 10;
                        }
                        if (calculatedMap.containsKey(key)) {
                            long currentDuration = calculatedMap.get(key);
                            currentDuration += timeToAdd;
                            calculatedMap.put(key, currentDuration);
                        } else {
                            long newDuration = timeToAdd;
                            calculatedMap.put(key, newDuration);
                        }
                    }
                    timeDiffInSeconds = 0;
                    name1 = name2;
                    timestamp1 = timestamp2;
                    appCat1 = appCat2;
                }
            } else {
                String[] earlier = ownerList.get(0);
                String name = earlier[0];
                String timestamp = earlier[1];
                String appCat = earlier[2];
                if (appCat.equalsIgnoreCase(chosenFilter)) {
                    String key = currentMacAdd + ", " + name;
                    long newDuration = 10;
                    calculatedMap.put(key, newDuration);
                }

            }
        }
        Set<String> macNameSet = calculatedMap.keySet();
        LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<String, Long>();
        Iterator<String> toIterMacName = macNameSet.iterator();
        long longestDuration = 0;
        String macNameWithLongestDuration = "";
        int mapCount = macNameSet.size();
        for (int i = 0; i < mapCount; i++) {
            macNameSet = calculatedMap.keySet();
            toIterMacName = macNameSet.iterator();
            while (toIterMacName.hasNext()) {
                String currentAppName = toIterMacName.next();
                long currentDuration = calculatedMap.get(currentAppName);
                if (currentDuration > longestDuration) {
                    longestDuration = currentDuration;
                    macNameWithLongestDuration = currentAppName;
                }
            }
            sortedMap.put(macNameWithLongestDuration, longestDuration);
            calculatedMap.remove(macNameWithLongestDuration);
            longestDuration = 0;
            macNameWithLongestDuration = "";
        }
        LinkedHashMap<Long, String> consolMap = new LinkedHashMap<Long, String>();
        LinkedHashMap<String, Long> toReturn = new LinkedHashMap<String, Long>();
        Set<String> keysForSortedMap = sortedMap.keySet();
        Iterator<String> toIterSortedMap = keysForSortedMap.iterator();

        /*String currentAppName = toIterSortedMap.next();
         long currentDuration = sortedMap.get(currentAppName);
         String finalAppName = currentAppName;*/
        int count = 0;

        while (toIterSortedMap.hasNext() && count < kValueInt) {
            String appName = toIterSortedMap.next();
            long dur = sortedMap.get(appName);
            if (consolMap.get(dur) != null) {
                String curName = consolMap.get(dur);
                curName = curName + ", " + appName;
                consolMap.put(dur, curName);
            } else {
                consolMap.put(dur, appName);
            }
            count++;
        }

        Set<Long> consolMapKeys = consolMap.keySet();
        Iterator<Long> iterCMK = consolMapKeys.iterator();
        while (iterCMK.hasNext()) {
            long k = iterCMK.next();
            String names = consolMap.get(k);
            toReturn.put(names, k);
        }
        return toReturn;
    }

    /**
     * Given a k value, category, start date and end date specified by user,
     * retrieve all relevant mac-address, name and timestamp from SQL and
     * process the Top schools with most app usage time
     *
     * @param kValueInt number of results specified by user
     * @param date1 start date specified by user
     * @param date2 end date specified by user
     * @param chosenFilter app category specified by user
     * @return a LinkedHashMap with school name as key and app usage time as
     * value
     * @throws IOException
     */
    public LinkedHashMap<String, Long> processSchoolGivenCat(int kValueInt, Date date1, Date date2, String chosenFilter) throws IOException {
        //HashMap Key: school name, Value: app usage duration
        topKAppDAO tkaDAO = new topKAppDAO();

        //LinkedHashMap with email as key and arrylist of string[] containing timestamp and app category as value
        LinkedHashMap<String, ArrayList<String[]>> emailTimeMap = tkaDAO.getTopKSchoolsGivenCat(date1, date2, chosenFilter);
        LinkedHashMap<String, Long> toReturn = new LinkedHashMap<String, Long>();
        LinkedHashMap<String, Long> calculatedMap = new LinkedHashMap<String, Long>();

        Date dateTime1 = null;
        Date dateTime2 = null;
        long timeDiffInSeconds = 0;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        /*String macAddress1 = "";
         String macAddress2 = "";
         String timestamp1 = "";
         String timestamp2 = "";
         String email2 = "";*/
        String email1 = "";
        String timeStamp1 = "";
        String timeStamp2 = "";
        Set<String> emailSet = emailTimeMap.keySet();
        String nextAppCat = "";
        String currentAppCat = "";

        if (!emailSet.isEmpty()) {
            Iterator<String> toIterateEmail = emailSet.iterator();

            while (toIterateEmail.hasNext()) {
                long totalDuration = 0;
                email1 = toIterateEmail.next();
                // arrylist of string[] containing timestamp and app category as value
                ArrayList<String[]> listTimeStamp1 = emailTimeMap.get(email1);

                if (listTimeStamp1.size() > 1) { // more than 1 timestamp entry
                    String[] earlier = listTimeStamp1.get(0);
                    String timestamp1 = earlier[0];
                    String appCat1 = earlier[1];
                    for (int i = 1; i < listTimeStamp1.size(); i++) {
                        String[] later = listTimeStamp1.get(i);
                        String timestamp2 = later[0];
                        String appCat2 = later[1];
                        String key = email1;
                        if (appCat1.equalsIgnoreCase(chosenFilter)) { //appCat1 is the chosen filter
                            try {
                                dateTime1 = format.parse(timestamp2);//later time 
                                dateTime2 = format.parse(timestamp1);// earlier time
                                long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                                timeDiffInSeconds = timeDiff / 1000;
                                if (timeDiffInSeconds > 120) {
                                    timeDiffInSeconds = 10;
                                }

                                if (calculatedMap.containsKey(key)) {
                                    long currentDuration = calculatedMap.get(key);
                                    currentDuration += timeDiffInSeconds;
                                    calculatedMap.put(key, currentDuration);
                                } else {
                                    long newDuration = timeDiffInSeconds;
                                    calculatedMap.put(key, newDuration);
                                }
                            } catch (ParseException ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (appCat2.equalsIgnoreCase(chosenFilter) && (i == listTimeStamp1.size() - 1)) {
                            long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                            if (timeToAdd > 10) {
                                timeToAdd = 10;
                            }
                            if (calculatedMap.containsKey(key)) {
                                long currentDuration = calculatedMap.get(key);
                                currentDuration += timeToAdd;
                                calculatedMap.put(key, currentDuration);
                            } else {
                                long newDuration = timeToAdd;
                                calculatedMap.put(key, newDuration);
                            }
                        }
                        timeDiffInSeconds = 0;
                        
                        timestamp1 = timestamp2;
                        appCat1 = appCat2;
                    }
                } else {
                    String[] earlier = listTimeStamp1.get(0);
                    String name = earlier[0];
                    String timestamp = earlier[1];
                    String appCat = earlier[2];
                    if (appCat.equalsIgnoreCase(chosenFilter)) {
                        String key = email1;
                        long newDuration = 10;
                        calculatedMap.put(key, newDuration);
                    }

                }

                /*if (listTimeStamp1.size() != 0) {
                 String[] currentArr = listTimeStamp1.get(0);
                 timeStamp1 = currentArr[0];
                 currentAppCat = currentArr[1];
                 }
                 for (int i = 1; i < listTimeStamp1.size(); i++) {
                 String[] currentArr = listTimeStamp1.get(i);
                 timeStamp2 = currentArr[0];
                 nextAppCat = currentArr[1];

                 if (currentAppCat.equals(chosenFilter)) {
                 try {
                 dateTime1 = format.parse(timeStamp1);//earlier time 
                 dateTime2 = format.parse(timeStamp2);// later time
                 long timeDiff = dateTime2.getTime() - dateTime1.getTime();
                 timeDiffInSeconds = timeDiff / 1000;
                 if (timeDiffInSeconds > 120) {
                 timeDiffInSeconds = 10;
                 }
                 if (i == listTimeStamp1.size() - 1) {
                 if (nextAppCat.equals(chosenFilter)) {
                 long timeToAdd = (date2.getTime() - dateTime2.getTime()) / 1000 * 1;
                 if (timeToAdd > 10) {
                 timeToAdd = 10;
                 }
                 totalDuration += timeToAdd;
                 }
                 }
                 totalDuration += timeDiffInSeconds;
                 timeStamp1 = timeStamp2;
                 } catch (ParseException ex) {
                 ex.printStackTrace();
                 }
                 }
                 }
                 calculatedMap.put(email1, totalDuration);*/
            }
        }

        LinkedHashMap<String, Long> sortedMap = new LinkedHashMap<String, Long>();

        Set<String> calcMapSet = calculatedMap.keySet();
        Iterator<String> toIterCalMap = calcMapSet.iterator();
        long finalDuration = 0;

        while (toIterCalMap.hasNext()) {
            String currentEmail = toIterCalMap.next();
            String[] splitCurrentEmail = currentEmail.split("@");
            String schoolEmail1 = splitCurrentEmail[1];
            String[] splitSchoolEmail1 = schoolEmail1.split("\\.");
            String school = splitSchoolEmail1[0];

            long duration = calculatedMap.get(currentEmail);

            if (sortedMap.get(school) != null) {
                long durInside = sortedMap.get(school);
                durInside += duration;
                sortedMap.put(school, durInside);
            } else {
                sortedMap.put(school, duration);
            }
        }

        /* String currentEmail = toIterCalMap.next();
         String[] splitCurrentEmail = currentEmail.split("@");
         String schoolEmail1 = splitCurrentEmail[1];
         String[] splitSchoolEmail1 = schoolEmail1.split("\\.");
         String school = splitSchoolEmail1[0];

         long duration = calculatedMap.get(currentEmail);

         while (toIterCalMap.hasNext()) {
         String nextEmail = toIterCalMap.next();
         String[] splitEmail2 = nextEmail.split("@");
         String schoolEmail2 = splitEmail2[1];
         String[] splitSchoolEmail2 = schoolEmail2.split("\\.");
         String school2 = splitSchoolEmail2[0];
         long nextDuration = calculatedMap.get(nextEmail);
         if (school.equals(school2)) {
         duration += nextDuration;
         school = school2;
         } else {
         sortedMap.put(school, duration);
         duration = nextDuration;
         school = school2;
         }
         }
         sortedMap.put(school, duration);*/
        LinkedHashMap<String, Long> superMap = new LinkedHashMap<String, Long>();
        Set<String> schoolInCalMap = sortedMap.keySet();
        long longestDuration = 0;
        String schoolWithLongestDuration = "";
        for (int i = 0; i < schoolInCalMap.size(); i++) {
            Iterator<String> toIterateSchool = schoolInCalMap.iterator();

            while (toIterateSchool.hasNext()) {
                String currentSchoolName = toIterateSchool.next();

                long currentDuration = sortedMap.get(currentSchoolName);
                if (currentDuration > longestDuration) {
                    longestDuration = currentDuration;
                    schoolWithLongestDuration = currentSchoolName;
                }
            }
            superMap.put(schoolWithLongestDuration, longestDuration);
            sortedMap.remove(schoolWithLongestDuration);
            sortedMap.put("" + i, (long) 0);
            longestDuration = 0;
            schoolWithLongestDuration = "";
        }

        Set<String> keysForSortedMap = superMap.keySet();// sorted from highes to lowest app usage duration
        Iterator<String> toIterSortedMap = keysForSortedMap.iterator();
        LinkedHashMap<Long, String> consolMap = new LinkedHashMap<Long, String>();

        int count = 0;

        while (toIterSortedMap.hasNext() && count < kValueInt) {
            String schName = toIterSortedMap.next();
            long dur = superMap.get(schName);
            if (consolMap.get(dur) != null) {
                String curName = consolMap.get(dur);
                curName = curName + ", " + schName;
                consolMap.put(dur, curName);
            } else {
                consolMap.put(dur, schName);
            }
            count++;
        }

        Set<Long> consolMapKeys = consolMap.keySet();
        Iterator<Long> iterCMK = consolMapKeys.iterator();
        while (iterCMK.hasNext()) {
            long k = iterCMK.next();
            String names = consolMap.get(k);
            toReturn.put(names, k);
        }

        /*String currentSchoolName = toIterSortedMap.next();
        long currentDuration = superMap.get(currentSchoolName);
        String finalSchoolName = currentSchoolName;

        int count = 0;
        while (toIterSortedMap.hasNext() && count < kValueInt) {

            String nextSchoolName = toIterSortedMap.next();
            long nextDuration = superMap.get(nextSchoolName);
            if (currentDuration == nextDuration) {

                finalSchoolName = finalSchoolName + ", " + nextSchoolName;
                count++;
            } else {

                toReturn.put(finalSchoolName, currentDuration);
                count++;
                currentDuration = nextDuration;
                currentSchoolName = nextSchoolName;
                finalSchoolName = currentSchoolName;
            }
        }
        toReturn.put(finalSchoolName, currentDuration);*/

        return toReturn;
    }

}
