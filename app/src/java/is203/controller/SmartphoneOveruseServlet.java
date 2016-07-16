/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.controller;

import is203.dao.SmartphoneOveruseDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "SmartphoneOveruseServlet", urlPatterns = {"/SmartphoneOveruseServlet"})
public class SmartphoneOveruseServlet extends HttpServlet {

    Date date1;
    Date date2;
    Calendar dateToday;
    SmartphoneOveruseDAO sDao = new SmartphoneOveruseDAO();

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SmartphoneOveruseServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SmartphoneOveruseServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        processRequest(request, response);
    }

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
        String[] result;
        String startDateInput = request.getParameter("startdate");
        String endDateInput = request.getParameter("enddate");
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
                request.setAttribute("smartError", message);
                RequestDispatcher view = request.getRequestDispatcher("SmartphoneOveruseForm.jsp");
                view.forward(request, response);
                return;
            } else if (date2.after(dateToday.getTime())) {
                date2 = dateToday.getTime();
            }
            // Retrieves the current user's email
            String studentUser = (String) request.getSession().getAttribute("email");


            // Retrieves the current user's mac_address
            String macAdd = sDao.retrieveMacAdd(studentUser);
            result = calculateSPO(macAdd, date1, date2, request, response);
            request.setAttribute("smartPhoneOveruse", result);
            RequestDispatcher view = request.getRequestDispatcher("SmartphoneOveruseReport.jsp");
            view.forward(request, response);
            return;
        } catch (ParseException ex) {
            String message = "Please fill in both Start Date and End Date.";
            request.setAttribute("smartError", message);
            RequestDispatcher view = request.getRequestDispatcher("SmartphoneOveruseForm.jsp");
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
        return "Smartphone Overuse Servlet";
    }// </editor-fold>

    /**
     * Given number of days, end date and all the timestamps, calculate the average daily usage
     * @param numOfDays number of days selected by user (end date - start date)
     * @param timestamps timestamps within the start date & end date defined by user
     * @param endDate end date defined by user
     * @return a double of the average daily usage
     */
    public double generateAverageDailyUsage(int numOfDays, ArrayList<String[]> timestamps, Date endDate) {
        String ts1 = "";
        String ts2 = "";
        Date dateTime1 = null;
        Date dateTime2 = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try {
            String[] first = timestamps.get(0);
            ts1 = first[0];
        } catch (NullPointerException npe) {
            return 0;
        } catch (IndexOutOfBoundsException e) {
            return 0;
        }
        long totalTimeOnSP = 0;
        if(timestamps.size()==1){
            try{
                dateTime2 = format.parse(ts1);
                }catch(ParseException pe){
                    
                }
                long timeToAdd = (endDate.getTime() - dateTime2.getTime())/1000*-1;
                    if(timeToAdd < -10){
                        
                        timeToAdd = -10;
                    }
                    totalTimeOnSP +=timeToAdd;
        }
        for (int i = 1; i < timestamps.size(); i++) {
            String[] second = timestamps.get(i);
            ts2 = second[0];
            if (ts1.equals(ts2)) {
                //does nothing since it is still the first entry of ArrayList
            } else {
                try {
                    dateTime1 = format.parse(ts1);
                    dateTime2 = format.parse(ts2);
                } catch (ParseException ex) {

                }

                long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                long timeDiffInSeconds = timeDiff / 1000;

                if (timeDiffInSeconds < -120) {
                    timeDiffInSeconds = -10;
                }
                if(i == timestamps.size()-1){
                    long timeToAdd = (endDate.getTime() - dateTime2.getTime())/1000*-1;
                    if(timeToAdd < -10){
                        
                        timeToAdd = -10;
                    }
                    totalTimeOnSP += timeToAdd;
                }
                totalTimeOnSP += timeDiffInSeconds;

                ts1 = ts2;

            }
        }
        double average = totalTimeOnSP / numOfDays;
        return average * -1;
        
    }

    /**
     * Given number of days, end date and all the timestamps, calculate the average game usage
     * @param numOfDays number of days selected by user (end date - start date)
     * @param timestamps timestamps that falls within the range of start date& end date specified by user
     * @param endDate as specified by user
     * @return a double of average game usage
     */
    public double generateAverageGameUsage(int numOfDays, ArrayList<String> timestamps, Date endDate) {
        String ts1 = "";
        String ts2 = "";
        Date dateTime1 = null;
        Date dateTime2 = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long totalTimeOnGame = 0;
        if (timestamps.size() == 0) {
            return 0;
        } else {
            ts1 = timestamps.get(0);
            if(timestamps.size()==1){
                try{
                dateTime2 = format.parse(ts1);
                }catch(ParseException pe){
                    
                }
                long timeToAdd = (endDate.getTime() - dateTime2.getTime())/1000*-1;
                    if(timeToAdd < -10){
                        
                        timeToAdd = -10;
                    }
                    totalTimeOnGame +=timeToAdd;
            }
            for (int i = 1; i < timestamps.size(); i++) {
                ts2 = timestamps.get(i);
                try {
                    dateTime1 = format.parse(ts1);
                    dateTime2 = format.parse(ts2);

                } catch (ParseException pe) {

                }
                long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                long timeDiffInSeconds = timeDiff / 1000;

                if (timeDiffInSeconds < -120) {
                    timeDiffInSeconds = -10;
                }
                if(i == timestamps.size()-1){
                    long timeToAdd = (endDate.getTime() - dateTime2.getTime())/1000*-1;
                    if(timeToAdd < -10){
                        
                        timeToAdd = -10;
                    }
                    totalTimeOnGame += timeToAdd;
                }
                totalTimeOnGame += timeDiffInSeconds;
                ts1 = ts2;
            }
            double averageSeconds = totalTimeOnGame / numOfDays;
            return (averageSeconds * -1);
        }
    }

    /**
     * Given the number of days and timestamps that falls within the start date and end date specified 
     * by user, calculate the smartphone frequency
     * @param numOfDays (end date - start date)+1 specified by user
     * @param timestamps timestamps that falls within start date and end date specified by user
     * @return a double of the smartphone frequency
     */
    public double generateSPFrequency(int numOfDays, ArrayList<String[]> timestamps) {
        String ts1 = "";
        String ts2 = "";
        Date dateTime1 = null;
        Date dateTime2 = null;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        double countInHour = 0;
        if (timestamps.size() == 0) {
            return 0;
        } else {
            String[] first = timestamps.get(0);
            ts1 = first[0];
            if (first != null) {
                countInHour++;
                for (int i = 1; i < timestamps.size(); i++) {

                    String[] second = timestamps.get(i);
                    ts2 = second[0];

                    try {
                        dateTime1 = format.parse(ts1);
                        dateTime2 = format.parse(ts2);
                    } catch (ParseException pe) {

                    }

                    long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                    long timeDiffInSeconds = timeDiff / 1000;

                    Calendar c1 = Calendar.getInstance();
                    Calendar c2 = Calendar.getInstance();

                    c1.setTime(dateTime1);
                    c2.setTime(dateTime2);
                    int hour1 = c1.get(Calendar.HOUR);
                    int hour2 = c2.get(Calendar.HOUR);

                    if (timeDiffInSeconds < -120 && hour1 == hour2) {
                        countInHour++;
                    }
                    if (hour1 != hour2) {
                        countInHour++;
                    }
                    ts1 = ts2;
                }
                return countInHour;
            } else {
                return 0;
            }
        }
    }

    /**
     * Given start date and end date specified by user, calculate the smartphone overuse
     * @param macAdd mac address of user
     * @param startDate start date specified by user
     * @param endDate end date specified by user
     * @param request Servlet request
     * @param  response Servlet reponse
     * @return  String[] containing Final Index, Smartphone Index, daily usage, Game Index, daily gaming usage, Frequency Index, Average Frequency
     */
    public String[] calculateSPO(String macAdd, Date startDate, Date endDate, HttpServletRequest request, HttpServletResponse response) {

        String[] toReturn = null;
        date1 = startDate;
        date2 = endDate;

        // Retrieves all the timestamps by this user
        ArrayList<String[]> userList = sDao.retrieveTimestamps(macAdd, date1, date2);

        // Calculates the number of days the user wishes to track
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24) + 1);

        // Calculates average daily use
        double dailyUsageSeconds = generateAverageDailyUsage(days, userList, endDate);
        double dailyUsageHours = dailyUsageSeconds / 3600;

        String SPindex = "";
        if (dailyUsageHours >= 5) {
            SPindex = "Severe";
        } else if (dailyUsageHours >= 3 && dailyUsageHours < 5) {
            SPindex = "Moderate";
        } else {
            SPindex = "Light";
        }
        //retrieve all the timestamps with app category games
        ArrayList<String> gameUserList = sDao.retrieveGameTimeStamps(macAdd, date1, date2);
        
        double dailyGameUsageSeconds = generateAverageGameUsage(days, gameUserList, endDate);
        double dailyGameUsageHours = dailyGameUsageSeconds / 3600;
        String gameIndex = "";
        if (dailyGameUsageHours >= 2) {
            gameIndex = "Severe";
        } else if (dailyGameUsageHours >= 1 && dailyGameUsageHours < 2) {
            gameIndex = "Moderate";
        } else {
            gameIndex = "Light";
        }

        double smartPhoneFrequency = generateSPFrequency(days, userList);
        double averageFrequency = smartPhoneFrequency / (days * 24);
        String frequencyIndex = "";
        if (averageFrequency >= 5) {
            frequencyIndex = "Severe";
        } else if (averageFrequency >= 3 && averageFrequency < 5) {
            frequencyIndex = "Moderate";
        } else {
            frequencyIndex = "Light";
        }

        String finalIndex = "";
        int counterSevere = 0;
        if (SPindex.equals("Severe")) {
            counterSevere++;
        }
        if (gameIndex.equals("Severe")) {
            counterSevere++;
        }
        if (frequencyIndex.equals("Severe")) {
            counterSevere++;
        }

        int counterLight = 0;
        if (SPindex.equals("Light")) {
            counterLight++;
        }
        if (gameIndex.equals("Light")) {
            counterLight++;
        }
        if (frequencyIndex.equals("Light")) {
            counterLight++;
        }

        if (counterSevere >= 1) {
            finalIndex = "Overusing";
        } else if (counterLight == 3) {
            finalIndex = "Normal";
        } else {
            
            finalIndex = "ToBeCautious";
        }
        DecimalFormat df = new DecimalFormat("#0");
        DecimalFormat df2 = new DecimalFormat("#0.00");
        String dailyUsageSecondsString = df.format(dailyUsageSeconds);
        String dailyGameUsageSecondsString = df.format(dailyGameUsageSeconds);
        String averageFrequencyString = df2.format(averageFrequency);

        toReturn = new String[]{finalIndex, SPindex, dailyUsageSecondsString, gameIndex, dailyGameUsageSecondsString, frequencyIndex, averageFrequencyString};

        return toReturn;
    }
}
