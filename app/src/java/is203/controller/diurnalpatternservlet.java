
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.controller;

import is203.dao.breakdownDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
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

/**
 *
 * @author User
 */
@WebServlet(name = "diurnalpatternservlet", urlPatterns = {"/diurnalpatternservlet"})
public class diurnalpatternservlet extends HttpServlet {

    breakdownDAO bDAO = new breakdownDAO();

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
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        String dateInput = request.getParameter("diurnalpattern");
        String year = request.getParameter("year");
        String gender = request.getParameter("gender");
        String school = request.getParameter("school");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateEarly;
        String message = "";

        //if got no date input, return to page with error message
        if (dateInput.equals("")) {
            message = "Please fill in a date!";
            request.setAttribute("errorMsg", message);
            RequestDispatcher view = request.getRequestDispatcher("basicappusageform.jsp");
            view.forward(request, response);
            return;
        }
        try {
            dateEarly = sdf.parse(dateInput);
            ArrayList<String[]> toReturn = diurnalResults(dateEarly, year, gender, school);
            //iterate through 25 times to get timings
            request.setAttribute("diurnalreport", toReturn);
            RequestDispatcher view = request.getRequestDispatcher("basicappusagereport.jsp");
            view.forward(request, response);
            return;

        } catch (ParseException e) {
            e.printStackTrace();
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }

    }

    /**
     * Given specified date by user, retrieve relevant mac address and timestamp from breakdown 
     * DAO then calculate the usage based on each hour and whether there is specific demographic
     * requirements
     * @param dateEarly date specified by user
     * @param year parameter chosen by user (if any)
     * @param gender parameter chosen by user (if any)
     * @param school parameter chosen by user (if any)
     * @return an ArrayList of String[] containing the start time, end time app usage time in seconds
     * @throws IOException
     * @throws ParseException 
     */
    public ArrayList<String[]> diurnalResults(Date dateEarly, String year, String gender, String school) throws ParseException, IOException {
        String query = "";
        Date dateTimeEarlier;
        Date dateTimeLater;
        int toAdd;
        double averageNumSeconds = 0;
        ArrayList<String[]> toReturn = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#0");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar c = Calendar.getInstance();
        c.setTime(dateEarly);
        c.add(Calendar.DATE, 1);
        Date maxTime = c.getTime();
        maxTime = new Date(maxTime.getTime() - 1000);
        int numUsersDiurnalPattern = bDAO.retrieveNumMacAdd(dateEarly, maxTime);
        ArrayList<String> numUsersDiurnalPatternDemo = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            ArrayList<String[]> result = new ArrayList<>();
            double totalSeconds = 0;
            String macAdd = "";
            String timeStamp = "";
            String macAddToCompare = "";
            String timeStampToCompare = "";
            c.setTime(dateEarly);
            c.add(Calendar.HOUR, 1);
            Date timeStampMaximum = c.getTime();
            long timeStampMax = timeStampMaximum.getTime();
            
            //get maxtime for very hour 00:59:59, 01:59:59
            Date timeStampForQuery = new Date(timeStampMax - 1000);
            
            //if no filters
            if ((year.equals("") && gender.equals("") && school.equals(""))||( year.equals("NA")&&gender.equals("NA")&&school.equals("NA"))) {
                result = bDAO.retrieveWithinStartEndDiurnalPattern(dateEarly, timeStampForQuery);
                if (!result.isEmpty()) {
                    for (int j = 0; j < result.size(); j++) {
                        String[] values = result.get(j);
                        macAdd = values[0];
                        timeStamp = values[1];
                        //check if macAdd are the same for two occurances
                        if (macAdd.equals(macAddToCompare)) {
                            dateTimeEarlier = format.parse(timeStampToCompare);
                            dateTimeLater = format.parse(timeStamp);
                            //change to seconds
                            long timeDifference = dateTimeLater.getTime() - dateTimeEarlier.getTime();
                            long timeDiffInSeconds = timeDifference / 1000;
                            toAdd = (int) timeDiffInSeconds;
                            //if difference between two timestamps > 120, no more interaction with app
                            if (timeDiffInSeconds > 120) {
                                toAdd = 10;
                                totalSeconds += toAdd;
                            } //if difference between two timestamps < 120, interaction with app
                            else {
                                totalSeconds += toAdd;
                            }

                        } else if (!macAdd.equals(macAddToCompare) && !macAddToCompare.equals("")) {
                            dateTimeEarlier = format.parse(timeStampToCompare);
                            long difference = (timeStampMaximum.getTime() - dateTimeEarlier.getTime()) / 1000;
                            int diff = (int) difference;
                            if (diff > 10) {
                                toAdd = 10;
                                totalSeconds += toAdd;
                            } else {
                                totalSeconds += diff;
                            }
                        }

                        if (j == (result.size() - 1)) {
                            dateTimeEarlier = format.parse(timeStamp);
                            long difference = (timeStampMaximum.getTime() - dateTimeEarlier.getTime()) / 1000;
                            int diff = (int) difference;
                            if (diff > 10) {
                                toAdd = 10;
                                totalSeconds += toAdd;

                            } else {
                                totalSeconds += diff;
                            }
                        }
                        macAddToCompare = macAdd;
                        timeStampToCompare = timeStamp;
                    }

                    averageNumSeconds = (totalSeconds / numUsersDiurnalPattern);
                    String seconds = "" + df.format(averageNumSeconds);
                    String timeEarly = dateEarly.toString().substring(11, 16);
                    String timeLater = timeStampMaximum.toString().substring(11, 16);
                    toReturn.add(new String[]{timeEarly, timeLater, seconds});
                    c.setTime(dateEarly);
                    c.add(Calendar.HOUR, 1);
                    dateEarly = c.getTime();
                } else {
                    String seconds = "" + 0;
                    String timeEarly = dateEarly.toString().substring(11, 16);
                    String timeLater = timeStampMaximum.toString().substring(11, 16);
                    toReturn.add(new String[]{timeEarly, timeLater, seconds});
                    c.setTime(dateEarly);
                    c.add(Calendar.HOUR, 1);
                    dateEarly = c.getTime();
                }
            }
            //if got filters
            if ((!year.equals("") && !year.equals("NA")) || (!gender.equals("") && !gender.equals("NA")) || (!school.equals("") && !school.equals("NA"))) {
                if ((gender.equals("")||gender.equals("NA")) && (school.equals("")||school.equals("NA"))) {
                    query = year;
                } else if ((year.equals("")||year.equals("NA")) && (school.equals("")||school.equals("NA"))) {
                    query = gender;
                } else if ((gender.equals("")||gender.equals("NA")) && (year.equals("")||year.equals("NA"))) {
                    query = school;
                } else if (gender.equals("")||gender.equals("NA")) {
                    query = year + "," + school;
                } else if (year.equals("")||year.equals("NA")) {
                    query = school + "," + gender;
                } else if (school.equals("")||school.equals("NA")) {
                    query = year + "," + gender;
                } else {
                    query = year + "," + school + "," + gender;
                }
                result = bDAO.retrieveWithinStartEndDiurnalPatternDemo(dateEarly, timeStampForQuery, query);
                if (!result.isEmpty()) {
                    for (int j = 0; j < result.size(); j++) {
                        String[] values = result.get(j);
                        macAdd = values[0];
                        if(!numUsersDiurnalPatternDemo.contains(macAdd)) {
                            numUsersDiurnalPatternDemo.add(macAdd);
                        }
                        timeStamp = values[1];
                        //check if macAdd are the same for two occurances
                        if (macAdd.equals(macAddToCompare)) {
                            dateTimeEarlier = format.parse(timeStampToCompare);
                            dateTimeLater = format.parse(timeStamp);
                            //change to seconds
                            long timeDifference = dateTimeLater.getTime() - dateTimeEarlier.getTime();
                            long timeDiffInSeconds = timeDifference / 1000;
                            toAdd = (int) timeDiffInSeconds;
                            //if difference between two timestamps > 120, no more interaction with app
                            if (timeDiffInSeconds > 120) {
                                toAdd = 10;
                                totalSeconds += toAdd;
                            } //if difference between two timestamps < 120, interaction with app
                            else {
                                totalSeconds += toAdd;
                            }

                        } else if (!macAdd.equals(macAddToCompare) && !macAddToCompare.equals("")) {
                            dateTimeEarlier = format.parse(timeStampToCompare);
                            long difference = (timeStampMaximum.getTime() - dateTimeEarlier.getTime()) / 1000;
                            int diff = (int) difference;
                            if (diff > 10) {
                                toAdd = 10;
                                totalSeconds += toAdd;
                            } else {
                                totalSeconds += diff;
                            }
                        }

                        if (j == (result.size() - 1)) {
                            dateTimeEarlier = format.parse(timeStamp);
                            long difference = (timeStampMaximum.getTime() - dateTimeEarlier.getTime()) / 1000;
                            int diff = (int) difference;
                            if (diff > 10) {
                                toAdd = 10;
                                totalSeconds += toAdd;

                            } else {
                                totalSeconds += diff;
                            }
                        }
                        macAddToCompare = macAdd;
                        timeStampToCompare = timeStamp;
                    }

                    averageNumSeconds = (totalSeconds / numUsersDiurnalPatternDemo.size());
                    String seconds = "" + df.format(averageNumSeconds);
                    String timeEarly = dateEarly.toString().substring(11, 16);
                    String timeLater = timeStampMaximum.toString().substring(11, 16);
                    toReturn.add(new String[]{timeEarly, timeLater, seconds});
                    c.setTime(dateEarly);
                    c.add(Calendar.HOUR, 1);
                    dateEarly = c.getTime();
                } else {
                    String seconds = "" + 0;
                    String timeEarly = dateEarly.toString().substring(11, 16);
                    String timeLater = timeStampMaximum.toString().substring(11, 16);
                    toReturn.add(new String[]{timeEarly, timeLater, seconds});
                    c.setTime(dateEarly);
                    c.add(Calendar.HOUR, 1);
                    dateEarly = c.getTime();
                }
            }
        }
        return toReturn;
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Diurnal  Pattern Servlet";
    }// </editor-fold>

}
