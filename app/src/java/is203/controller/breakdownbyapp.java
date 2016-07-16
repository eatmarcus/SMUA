/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.controller;

import is203.dao.breakdownDAO;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
 * @author User
 */
@WebServlet(name = "breakdownbyapp", urlPatterns = {"/breakdownbyapp"})
public class breakdownbyapp extends HttpServlet {

    ArrayList<String[]> result = new ArrayList<String[]>();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
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

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date1;
        Date date2;
        Calendar dateToday;
        String startDateInput = request.getParameter("startdate");
        String endDateInput = request.getParameter("enddate");
        if (startDateInput.equals("") || endDateInput.equals("")) {
            String message = "Please fill in both Start Date and End Date.";
            request.setAttribute("errorMsg", message);
            RequestDispatcher view = request.getRequestDispatcher("basicappusageform.jsp");
            view.forward(request, response);
        }
        try {
            date1 = sdf.parse(startDateInput);
            date2 = sdf.parse(endDateInput);
            dateToday = Calendar.getInstance();

            //If start date is after end date
            if (date1.after(date2)) {
                String message = "Start Date cannot be after End Date.";
                request.setAttribute("errorMsg", message);
                RequestDispatcher view = request.getRequestDispatcher("basicappusageform.jsp");
                view.forward(request, response);

            } else if (date2.after(dateToday.getTime())) {
                date2 = dateToday.getTime();
            }
            // return to page with attributes
            result = toUseForJson(date1, date2, request, response);
            request.setAttribute("BreakDownAppCat", result);
            RequestDispatcher view = request.getRequestDispatcher("basicappusagereport.jsp");
            view.forward(request, response);
            return;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (SQLException ex) {
            Logger.getLogger(breakdownbyapp.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Given start date and end date, retrieve from breakdownDAO mac address, timestamp and app 
     * category from SQL and process app usage time for each category
     * @param date1 start date specified by user
     * @param date2 end date specified by user
     * @param request Servlet request
     * @param response Servlet response
     * @return an ArrayLIst of String[] containing key (timing), app usage timing in seconds and app usage 
     * timing percentage
     * @throws IOException
     * @throws ServletException
     * @throws ParseException
     * @throws SQLException 
     */
    public ArrayList<String[]> toUseForJson(Date date1, Date date2, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException, ParseException, SQLException {

        Date dateTimeEarlier;
        Date dateTimeLater;
        String macAdd;
        String timeStamp;
        String category;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        int toAdd;
        double totaltime = 0;
        ArrayList<String[]> result = new ArrayList<>();
        ArrayList<String[]> breakDownResult = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#0");

        breakdownDAO bDAO = new breakdownDAO();

        Date startDate = date1;
        //endDate + 1 day -1 second
        Calendar c = Calendar.getInstance();
        c.setTime(date2);
        c.add(Calendar.DATE, 1);
        Date endDate = c.getTime();
        long t = endDate.getTime();
        endDate = new Date(t - 1000);
        HashMap<String, Integer> toReturn = new HashMap<String, Integer>();
        int noOfDays = (int) (((endDate.getTime() - startDate.getTime()) / (1000 * 60 * 60 * 24)) + 1);
        toReturn.put("Books", 0);
        toReturn.put("Social", 0);
        toReturn.put("Education", 0);
        toReturn.put("Entertainment", 0);
        toReturn.put("Information", 0);
        toReturn.put("Library", 0);
        toReturn.put("Local", 0);
        toReturn.put("Tools", 0);
        toReturn.put("Fitness", 0);
        toReturn.put("Games", 0);
        toReturn.put("Others", 0);
        for (int i = 0; i < noOfDays; i++) {
            String macAddToCompare = "";
        String timeStampToCompare = "";
        String categoryToCompare = "";
            c.setTime(startDate);
            c.add(Calendar.DATE, 1);
            Date timeMax = c.getTime();
            endDate = new Date(timeMax.getTime() - 1000);

            breakDownResult = bDAO.retrieveWithinStartEndByCategory(startDate, endDate);
            //Iterating through result set
            if (!breakDownResult.isEmpty()) {
                //for (String[] values : breakDownResult) {
                for (int j = 0; j < breakDownResult.size(); j++) {
                    String[] values = breakDownResult.get(j);
                    macAdd = values[0];
                    timeStamp = values[1];
                    category = values[2];

                    //Same user 
                    if (macAdd.equals(macAddToCompare)) {
                        dateTimeEarlier = format.parse(timeStampToCompare);
                        dateTimeLater = format.parse(timeStamp);
                        long timeDifference = dateTimeLater.getTime() - dateTimeEarlier.getTime();
                        long timeDiffInSeconds = timeDifference / 1000;
                        //cast to int so that can put in hashmap
                        toAdd = (int) timeDiffInSeconds;

                        if (toAdd > 120) {
                            //if more than 2mins in between, add 10 secs
                            toAdd = 10;
                            int value = toReturn.get(categoryToCompare);
                            value += toAdd;
                            toReturn.put(categoryToCompare, value);
                            //add the total time
                            totaltime += toAdd;
                        } else {
                            //if not more than 2mins in between
                            int value = toReturn.get(categoryToCompare);
                            value += toAdd;
                            toReturn.put(categoryToCompare, value);
                            //add the total time
                            totaltime += toAdd;
                        }

                    } else if (!macAdd.equals(macAddToCompare) && !macAddToCompare.equals("")) {
                        //if mac address is different and not the first occurance, add 10secs to the last occurance
                        int value = toReturn.get(categoryToCompare);
                        dateTimeEarlier = format.parse(timeStampToCompare);
                        long difference = (timeMax.getTime() - dateTimeEarlier.getTime() / 1000);
                        int diff = (int) difference;

                        if (diff > 10) {
                            toAdd = 10;
                            totaltime += toAdd;
                            value += toAdd;
                            toReturn.put(categoryToCompare, value);
                        } else {
                            totaltime += diff;
                            value += diff;
                            toReturn.put(categoryToCompare, value);
                        }
                    }
                    if (j == (breakDownResult.size() - 1)) {
                        int value = toReturn.get(category);
                        dateTimeEarlier = format.parse(timeStamp);

                        long difference = (timeMax.getTime() - dateTimeEarlier.getTime()) / 1000;
                        int diff = (int) difference;
                        if (diff > 10) {
                            toAdd = 10;
                            totaltime += toAdd;
                            value += toAdd;
                            toReturn.put(category, value);
                        } else {
                            totaltime += diff;
                            value += diff;
                            toReturn.put(category, value);
                        }
                    }

                    macAddToCompare = macAdd;
                    timeStampToCompare = timeStamp;
                    categoryToCompare = category;
                }
                c.setTime(startDate);
                c.add(Calendar.DATE, 1);
                startDate = c.getTime();
            }
        }

        Map<String, Integer> map = toReturn;
        //iterate through the map
        Iterator<Map.Entry<String, Integer>> entries = map.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, Integer> entry = entries.next();
            String key = entry.getKey();
            double value = entry.getValue();
            if (value == 0) {
                result.add(new String[]{key, "0", "0"});
            } else {
                String addToArrayListSeconds = "" + df.format((value / noOfDays));
                String addToArrayListPercentage = "" + df.format((entry.getValue() / totaltime) * 100);
                result.add(new String[]{key, addToArrayListSeconds, addToArrayListPercentage});
            }
        }
        ArrayList<String[]> result2 = new ArrayList<String[]>();
        TreeMap<String, String[]> toSort = new TreeMap<String, String[]>();
        for(int i = 0 ; i < result.size(); i++){
            String[] temp = result.get(i);
            String tempKey = temp[0];
            toSort.put(tempKey, temp);
        }
        Iterator iter = toSort.keySet().iterator();
        while(iter.hasNext()){
            String[] temp = toSort.get(iter.next());
            result2.add(temp);
        }
        return result2;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
