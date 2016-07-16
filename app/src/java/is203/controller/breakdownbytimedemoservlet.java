 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.controller;

import is203.dao.ConnectionManager;
import is203.dao.DemographicsDAO;
import is203.dao.breakdownDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author User
 */
@WebServlet(name = "breakdownbytimedemoservlet", urlPatterns = {"/breakdownbytimedemoservlet"})
public class breakdownbytimedemoservlet extends HttpServlet {

    private double entries;
    Date date1;
    Date date2;
    Calendar dateToday;
    breakdownDAO bDAO = new breakdownDAO();

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String startDateInput = request.getParameter("startdate");
        String endDateInput = request.getParameter("enddate");
        String sortBy1 = request.getParameter("firstChoice");
        String sortBy2 = request.getParameter("secondChoice");
        String sortBy3 = request.getParameter("thirdChoice");
        String sortBy4 = request.getParameter("fourthChoice");
        ArrayList<LinkedHashMap<String, ArrayList<String[]>>> toReturn = null;

        PrintWriter out = response.getWriter();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        try {
            date1 = sdf.parse(startDateInput);
            date2 = sdf.parse(endDateInput);
            dateToday = Calendar.getInstance();

            Calendar c = Calendar.getInstance();
            c.setTime(date2);
            c.add(Calendar.DATE, 1);
            date2 = c.getTime();
            long t = date2.getTime();
            date2 = new Date(t - 1000);

            // Instance 1: Date 1 after Date 2
            // Instance 2: Date 2 after today's date
            if (date1.after(date2)) {
                String message = "Start Date cannot be after End Date.";
                request.setAttribute("errorMsg", message);
                RequestDispatcher view = request.getRequestDispatcher("basicappusageform.jsp");
                view.forward(request, response);
                return;
            } else if (date2.after(dateToday.getTime())) {
                date2 = dateToday.getTime();

            }

            //int sort1;//Integer.parseInt(sortBy1);
            if (sortBy1.equals("0")) {
                String[] breakdownTime = processBreakDownByTime(date1, date2, request, response);
                request.setAttribute("result", breakdownTime);
                RequestDispatcher view = request.getRequestDispatcher("basicappusagereport.jsp");
                view.forward(request, response);
                return;
            } else if (sortBy2.equals("00")) {
                toReturn = processBreakDownByTimeDemo(date1, date2, sortBy1, request, response);

            } else if (sortBy3.equals("000")) {
                toReturn = processBreakDownByTimeDemo(date1, date2, sortBy2, request, response);
            } else if (sortBy4.equals("0000")) {
                toReturn = processBreakDownByTimeDemo(date1, date2, sortBy3, request, response);
            } else {
                toReturn = processBreakDownByTimeDemo(date1, date2, sortBy4, request, response);
            }

            LinkedHashMap<String, ArrayList<String[]>> breakdownby1 = toReturn.get(0);
            LinkedHashMap<String, ArrayList<String[]>> breakdownby2 = toReturn.get(1);
            LinkedHashMap<String, ArrayList<String[]>> breakdownby3 = toReturn.get(2);
            LinkedHashMap<String, ArrayList<String[]>> breakdownby4 = toReturn.get(3);
            LinkedHashMap<String, Integer> bauTimeDemoResults = new LinkedHashMap<String, Integer>();
            LinkedHashMap<String, ArrayList<String[]>> tempMap = new LinkedHashMap<String, ArrayList<String[]>>();

            if (breakdownby4 != null) { //filtered by 4
                tempMap = breakdownby4;
            } else if (breakdownby3 != null) { //filtered by 3
                tempMap = breakdownby3;
            } else if (breakdownby2 != null) { // filtered by 2
                tempMap = breakdownby2;
            } else { //filtered by 1
                tempMap = breakdownby1;
            }

            Set<String> keyTempMap = tempMap.keySet();
            Iterator<String> iterTempMap = keyTempMap.iterator();
            int intense = 0;
            int normal = 0;
            int mild = 0;
            while (iterTempMap.hasNext()) {
                intense = 0;
                normal = 0;
                mild = 0;
                String key = iterTempMap.next();
                ArrayList<String[]> arr = tempMap.get(key); // String[]->{macAdd2, usageCat, school, year, gender2, cca2}
                for (String[] str : arr) {
                    String cat = str[1];
                    if (cat.equals("intense")) {
                        intense++;
                    } else if (cat.equals("normal")) {
                        normal++;
                    } else {
                        mild++;
                    }
                }

                bauTimeDemoResults.put(key + ";Mild", mild);
                bauTimeDemoResults.put(key + ";Normal", normal);
                bauTimeDemoResults.put(key + ";Intense", intense);
            }
            request.setAttribute("bauTimeDemoResults", bauTimeDemoResults);
            request.setAttribute("bauTimeDemoBreakdown1", breakdownby1);
            request.setAttribute("bauTimeDemoBreakdown2", breakdownby2);
            request.setAttribute("bauTimeDemoBreakdown3", breakdownby3);
            request.setAttribute("bauTimeDemoBreakdown4", breakdownby4);

            RequestDispatcher view = request.getRequestDispatcher("basicappusagereport.jsp");
            view.forward(request, response);
        } catch (ParseException ex) {
            String message = "Please fill in both Start Date and End Date.";
            request.setAttribute("errorMsg", message);
            RequestDispatcher view = request.getRequestDispatcher("basicappusageform.jsp");
            view.forward(request, response);
        } catch (SQLException ex) {
            Logger.getLogger(breakdownbytimedemoservlet.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     *
     * @param date1
     * @param date2
     * @param request
     * @param response
     * @return
     * @throws SQLException
     * @throws ParseException
     * @throws ServletException
     * @throws IOException
     */
    public String[] processBreakDownByTime(Date date1, Date date2, HttpServletRequest request, HttpServletResponse response) throws SQLException, ParseException, ServletException, IOException {
        PrintWriter out = response.getWriter();
        //logic to processBreakDownByTime
        //rs must be sorted by Mac-Address. then by time stamp.
        ArrayList<String[]> rs = bDAO.retrieveWithinStartEnd(date1, date2);

        double countIntenseUsers = 0;
        double countNormalUsers = 0;
        double countMildUsers = 0;
        double countUsers = 0;

        String macAdd1 = "";
        String macAdd2 = "";
        String timeStamp1 = "";
        String timeStamp2 = "";
        Date dateTime1 = null;
        Date dateTime2 = null;

        int noOfDays = (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24) + 1);

        long totalTimeOnSP = 0; //total time on smartphone
        HashMap<String, Long> result = new HashMap<String, Long>();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (rs == null) {
            out.println("RS IS NULL?!?!");
        } else {
            for (int i = 0; i < rs.size(); i++) {
                String[] first = rs.get(i);
                macAdd1 = first[0];
                timeStamp1 = first[1];
                dateTime1 = format.parse(timeStamp1);
                //2nd entry onwards & similar macAdd
                if (macAdd1.equals(macAdd2)) {
                    //calculate duration between the timeStamp1 and timeStamp2
                    // later time

                    long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                    long timeDiffInSeconds = timeDiff / 1000;

                    if (timeDiffInSeconds > 120) {
                        timeDiffInSeconds = 10;
                    }

                    totalTimeOnSP += timeDiffInSeconds;
                    //last one in the list.
                    if (i == rs.size() - 1) {
                        long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                        if (timeToAdd > 10) {
                            timeToAdd = 10;
                        }
                        totalTimeOnSP += timeToAdd;
                        double averageTimeOnSP = totalTimeOnSP / noOfDays;
                        if (averageTimeOnSP >= 18000) {
                            countIntenseUsers++;
                        } else if (averageTimeOnSP < 18000 && averageTimeOnSP >= 3600) {
                            countNormalUsers++;
                        } else {
                            countMildUsers++;
                        }
                    }

                } else if (!macAdd1.equals(macAdd2) && !macAdd2.equals("")) { //Different MacAdd
                    long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                    if (timeToAdd > 10) {
                        timeToAdd = 10;
                    }
                    totalTimeOnSP += timeToAdd;
                    double averageTimeOnSP = totalTimeOnSP / noOfDays;

                    totalTimeOnSP = 0;
                    if (averageTimeOnSP >= 18000) {
                        countIntenseUsers++;
                    } else if (averageTimeOnSP < 18000 && averageTimeOnSP >= 3600) {
                        countNormalUsers++;
                    } else {
                        countMildUsers++;
                    }
                }

                macAdd2 = macAdd1;
                timeStamp2 = timeStamp1;
                dateTime2 = dateTime1;
            }
        }

        countUsers = countIntenseUsers + countMildUsers + countNormalUsers;
        DecimalFormat df = new DecimalFormat("#0");

        double percentOfIntense = ((countIntenseUsers / countUsers) * 100);
        double percentOfMild = ((countMildUsers / countUsers) * 100);
        double percentOfNormal = ((countNormalUsers / countUsers) * 100);
        String countIntense = ((int) (countIntenseUsers)) + "";
        String countNormal = ((int) (countNormalUsers)) + "";
        String countMild = ((int) (countMildUsers)) + "";
        String intense = (df.format(percentOfIntense)) + "";
        String mild = (df.format(percentOfMild)) + "";
        String normal = (df.format(percentOfNormal)) + "";
        if (countUsers == 0) {
            intense = "0";
            normal = "0";
            mild = "0";
        }

        String[] breakdownTime = new String[]{countIntense, intense, countNormal, normal, countMild, mild};
        return breakdownTime;
    }

    public ArrayList<LinkedHashMap<String, ArrayList<String[]>>> processBreakDownByTimeDemo(Date date1, Date date2, String subQuery, HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, SQLException, ParseException {

        ArrayList<String[]> toProcess = bDAO.retrieveWithinTimeDemo(date1, date2); // contains alredy calculated intense/normal/mild
        LinkedHashMap<String, ArrayList<String[]>> map = new LinkedHashMap<String, ArrayList<String[]>>();
        LinkedHashMap<String, ArrayList<String[]>> breakdownby1 = null;
        LinkedHashMap<String, ArrayList<String[]>> breakdownby2 = null;
        LinkedHashMap<String, ArrayList<String[]>> breakdownby3 = null;
        LinkedHashMap<String, ArrayList<String[]>> breakdownby4 = null;
        map.put("All", toProcess);

        String first = "";
        String second = "";
        String third = "";
        String fourth = "";

        String[] filters = subQuery.split(",");
        if (filters.length == 4) {
            first = filters[0].trim();
            second = filters[1].trim();
            third = filters[2].trim();
            fourth = filters[3].trim();
        } else if (filters.length == 3) {
            first = filters[0].trim();
            second = filters[1].trim();
            third = filters[2].trim();
        } else if (filters.length == 2) {
            first = filters[0].trim();
            second = filters[1].trim();
        } else {
            first = filters[0].trim();
        }

        if (first.equals("year")) {
            breakdownby1 = sortByYear(map);
        } else if (first.equals("gender")) {
            breakdownby1 = sortByGender(map);
        } else if (first.equals("school")) {
            breakdownby1 = sortBySchool(map);
        } else if (first.equals("cca")) {
            breakdownby1 = sortByCca(map);
        }

        if (!second.equals("")) {
            switch (second) {
                case "year":
                    breakdownby2 = sortByYear(breakdownby1);
                    break;
                case "gender":
                    breakdownby2 = sortByGender(breakdownby1);
                    break;
                case "cca":
                    breakdownby2 = sortByCca(breakdownby1);
                    break;
                case "school":
                    breakdownby2 = sortBySchool(breakdownby1);
                    break;
            }
        }

        if (!third.equals("")) {
            switch (third) {
                case "year":
                    breakdownby3 = sortByYear(breakdownby2);
                    break;
                case "gender":
                    breakdownby3 = sortByGender(breakdownby2);
                    break;
                case "cca":
                    breakdownby3 = sortByCca(breakdownby2);
                    break;
                case "school":
                    breakdownby3 = sortBySchool(breakdownby2);
                    break;
            }
        }

        if (!fourth.equals("")) {
            switch (fourth) {
                case "year":
                    breakdownby4 = sortByYear(breakdownby3);
                    break;
                case "gender":
                    breakdownby4 = sortByGender(breakdownby3);
                    break;
                case "cca":
                    breakdownby4 = sortByCca(breakdownby3);
                    break;
                case "school":
                    breakdownby4 = sortBySchool(breakdownby3);
                    break;
            }
        }

       // LinkedHashMap<String, Integer> bauTimeDemoResults = new LinkedHashMap<String, Integer>();
        // LinkedHashMap<String, ArrayList<String[]>> tempMap = new LinkedHashMap<String, ArrayList<String[]>>();
      //  if (breakdownby4 != null) { //filtered by 4
        //  tempMap = breakdownby4;
        //  } else if (breakdownby3 != null) { //filtered by 3
        //     tempMap = breakdownby3;
        //  } else if (breakdownby2 != null) { // filtered by 2
        //     tempMap = breakdownby2;
        //  } else { //filtered by 1
        //      tempMap = breakdownby1;
        //  }
       // Set<String> keyTempMap = tempMap.keySet();
        // Iterator<String> iterTempMap = keyTempMap.iterator();
        // int intense = 0;
        //int normal = 0;
        // int mild = 0;
        // while (iterTempMap.hasNext()) {
        //    intense = 0;
        //    normal = 0;
        //    mild = 0;
        //   String key = iterTempMap.next();
        //   ArrayList<String[]> arr = tempMap.get(key); // String[]->{macAdd2, usageCat, school, year, gender2, cca2}
        //   for (String[] str : arr) {
        //      String cat = str[1];
        //      if (cat.equals("intense")) {
        //        intense++;
        //    } else if (cat.equals("normal")) {
        //        normal++;
        //   } else {
        //        mild++;
        //   }
        //   }
           // bauTimeDemoResults.put(key + ";Mild", mild);
        // bauTimeDemoResults.put(key + ";Normal", normal);
        // bauTimeDemoResults.put(key + ";Intense", intense);
        //  }
        ArrayList<LinkedHashMap<String, ArrayList<String[]>>> toReturn = new ArrayList<LinkedHashMap<String, ArrayList<String[]>>>();
        toReturn.add(breakdownby1);
        toReturn.add(breakdownby2);
        toReturn.add(breakdownby3);
        toReturn.add(breakdownby4);

        return toReturn;
        //request.setAttribute("bauTimeDemoResults", bauTimeDemoResults);
        //request.setAttribute("bauTimeDemoBreakdown1", breakdownby1);
        //request.setAttribute("bauTimeDemoBreakdown2", breakdownby2);
        //request.setAttribute("bauTimeDemoBreakdown3", breakdownby3);
        //request.setAttribute("bauTimeDemoBreakdown4", breakdownby4);

        //request.setAttribute("totalEntries", entries);
        //request.setAttribute("filters", subQuery);
        //RequestDispatcher view = request.getRequestDispatcher("basicappusagereport.jsp");
        //view.forward(request, response);
    }

    private LinkedHashMap<String, ArrayList<String[]>> sortByYear(LinkedHashMap<String, ArrayList<String[]>> map) {
        LinkedHashMap<String, ArrayList<String[]>> newMap = new LinkedHashMap<String, ArrayList<String[]>>();

        // String[]->{macAdd2, usageCat, school, year, gender2, cca2}
        Set<String> keys = map.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            ArrayList<String[]> list = map.get(key);
            ArrayList<String[]> year2011 = new ArrayList<String[]>();
            ArrayList<String[]> year2012 = new ArrayList<String[]>();
            ArrayList<String[]> year2013 = new ArrayList<String[]>();
            ArrayList<String[]> year2014 = new ArrayList<String[]>();
            ArrayList<String[]> year2015 = new ArrayList<String[]>();
            for (String[] sArr : list) {
                String yr = sArr[3];
                if (yr.equals("2011")) {
                    year2011.add(sArr);
                } else if (yr.equals("2012")) {
                    year2012.add(sArr);
                } else if (yr.equals("2013")) {
                    year2013.add(sArr);
                } else if (yr.equals("2014")) {
                    year2014.add(sArr);
                } else {
                    year2015.add(sArr);
                }

            }
            newMap.put((key + ";2011"), year2011);
            newMap.put((key + ";2012"), year2012);
            newMap.put((key + ";2013"), year2013);
            newMap.put((key + ";2014"), year2014);
            newMap.put((key + ";2015"), year2015);
        }
        return newMap;
    }

    private LinkedHashMap<String, ArrayList<String[]>> sortByGender(LinkedHashMap<String, ArrayList<String[]>> map) {
        LinkedHashMap<String, ArrayList<String[]>> newMap = new LinkedHashMap<String, ArrayList<String[]>>();
        // String[]->{macAdd2, usageCat, school, year, gender2, cca2}

        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            ArrayList<String[]> female = new ArrayList<String[]>();
            ArrayList<String[]> male = new ArrayList<String[]>();
            ArrayList<String[]> list = map.get(key);
            for (String[] sArr : list) {
                String gender = sArr[4];
                if (gender.equalsIgnoreCase("M")) {
                    male.add(sArr);
                } else {
                    female.add(sArr);
                }

            }
            newMap.put((key + ";Male"), male);
            newMap.put((key + ";Female"), female);
        }
        return newMap;
    }

    private LinkedHashMap<String, ArrayList<String[]>> sortByCca(LinkedHashMap<String, ArrayList<String[]>> map) {
        LinkedHashMap<String, ArrayList<String[]>> newMap = new LinkedHashMap<String, ArrayList<String[]>>();
        ArrayList<String> ccas = DemographicsDAO.retrieveAllCCA();
        Collections.sort(ccas);

        Set<String> keys = map.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            ArrayList<String[]> list = map.get(key);

            for (String cca : ccas) {
                ArrayList<String[]> results = new ArrayList<String[]>();
                for (String[] sArr : list) {
                    String thisCCA = sArr[5];
                    if (thisCCA.equals(cca)) {
                        results.add(sArr);
                    }
                }
                newMap.put(key + ";" + cca, results);
            }
        }
        return newMap;
    }

    private LinkedHashMap<String, ArrayList<String[]>> sortBySchool(LinkedHashMap<String, ArrayList<String[]>> map) {
        LinkedHashMap<String, ArrayList<String[]>> newMap = new LinkedHashMap<String, ArrayList<String[]>>();
        // String[]->{macAdd2, usageCat, school, year, gender2, cca2}

        Set<String> keys = map.keySet();
        Iterator<String> iter = keys.iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            ArrayList<String[]> list = map.get(key);
            ArrayList<String[]> accountancy = new ArrayList<String[]>();
            ArrayList<String[]> business = new ArrayList<String[]>();
            ArrayList<String[]> econs = new ArrayList<String[]>();
            ArrayList<String[]> law = new ArrayList<String[]>();
            ArrayList<String[]> sis = new ArrayList<String[]>();
            ArrayList<String[]> socsc = new ArrayList<String[]>();
            for (String[] sArr : list) {
                String sch = sArr[2];
                if (sch.equals("accountancy")) {
                    accountancy.add(sArr);
                } else if (sch.equals("business")) {
                    business.add(sArr);
                } else if (sch.equals("economics")) {
                    econs.add(sArr);
                } else if (sch.equals("sis")) {
                    sis.add(sArr);
                } else if (sch.equals("law")) {
                    law.add(sArr);
                } else {
                    socsc.add(sArr);
                }

            }
            newMap.put((key + ";Accountancy"), accountancy);
            newMap.put((key + ";Business"), business);
            newMap.put((key + ";Economics"), econs);
            newMap.put((key + ";Law"), law);
            newMap.put((key + ";SIS"), sis);
            newMap.put((key + ";SOCSC"), socsc);
        }
        return newMap;
    }
}
