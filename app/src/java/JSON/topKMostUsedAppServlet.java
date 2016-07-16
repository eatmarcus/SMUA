/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import is203.controller.processTopKAppServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.TreeMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "topKMostUsedAppServlet", urlPatterns = {"/json/top-k-most-used-apps"})
public class topKMostUsedAppServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    Date date1;
    Date date2;

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
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */

            ArrayList<String> errorList = new ArrayList<String>();
            JsonArray errorArr = new JsonArray();
            String currentToken = null;
            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String school = request.getParameter("school");
            String kString = request.getParameter("k");
            int kValue;
            String error = "";
            String token = request.getParameter("token");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (token == null) {
                errorList.add("missing token");
            } else if (request.getHeader("referer") == null) {

                currentToken = (String) request.getParameter("token");
            } else if (currentToken.equals("") || currentToken.isEmpty()) {
                errorList.add("blank token");
            } else {
                try {
                    JWTUtility.verify(currentToken, sharedSecretManager.getSharedSecret());
                    currentToken = (String) session.getAttribute("token");
                } catch (JWTException e) {
                    errorList.add("invalid token");
                }
            }
            if (endDate == null) {
                errorList.add("missing end date");
            } else if (endDate.equals("") || endDate.isEmpty()) {
                errorList.add("blank enddate");
            }
            if (startDate == null) {
                errorList.add("missing start date");
            } else if (startDate.equals("") || startDate.isEmpty()) {
                errorList.add("blank startdate");
            } else {
                try {
                    date1 = sdf.parse(startDate);
                } catch (ParseException ex) {
                    errorList.add("invalid startdate");
                }
                try {
                    date2 = sdf.parse(endDate);

                    Calendar c = Calendar.getInstance();
                    c.setTime(date2);
                    c.add(Calendar.DATE, 1);
                    date2 = c.getTime();
                    long t = date2.getTime();
                    date2 = new Date(t - 1000);

                    // Instance 1: Date 1 after Date 2
                    // Instance 2: Date 2 after today's date
                    if (date1.after(date2)) {
                        errorList.add("invalid startdate");
                    }

                    //int sort1;//Integer.parseInt(sortBy1);
                } catch (ParseException ex) {
                    errorList.add("invalid enddate");

                }
            }

            if (kString == null) {
                errorList.add("missing k value");
            } else if (Integer.parseInt(kString) < 0 || Integer.parseInt(kString) > 10) {
                errorList.add("Invalid k value");
            }

            if (errorList.size() != 0) {

                for (String s : errorList) {
                    JsonPrimitive element = new JsonPrimitive(s);
                    errorArr.add(element);
                }
                problems.addProperty("status", "error");

                problems.add("messages", errorArr);
                out.println(gson.toJson(problems));

            } else {

                TreeMap<String, Long> sortedResultInRank = new TreeMap<String, Long>();
                processTopKAppServlet pServlet = new processTopKAppServlet();
                LinkedHashMap<String, Long> resultMap = pServlet.processTopKGivenSchool(Integer.parseInt(kString), date1, date2, school);
                JsonArray resultArr = new JsonArray();
                Set<String> appNameSet = resultMap.keySet();
                int rank = 1;
                Iterator<String> appNameIter = appNameSet.iterator();
                while (appNameIter.hasNext()) {
                    String appName = appNameIter.next();
                    Long duration = resultMap.get(appName);
                    if (appName.contains(",")) {
                        String[] arrOfSameName = appName.split(",");
                        for (String s : arrOfSameName) {
                           
                            sortedResultInRank.put(s.trim(), duration);

                        }

                        Iterator<String> iterResult = sortedResultInRank.keySet().iterator();
                        String firstAppName = "";
                        while (iterResult.hasNext()) {
                            firstAppName = iterResult.next();
                            
                            JsonObject sameTemp = new JsonObject();
                            sameTemp.addProperty("rank", rank);
                            sameTemp.addProperty("app-name", firstAppName.trim());
                            sameTemp.addProperty("duration", duration);
                            resultArr.add(sameTemp);
                        }
                        if (appName.contains(",")) {
                            String[] nameArr = appName.split(",");
                            int number = nameArr.length;
                            rank += number - 1;
                        }
                    } else {
                        JsonObject temp = new JsonObject();
                        temp.addProperty("rank", rank);
                        temp.addProperty("app-name", appName);
                        temp.addProperty("duration", duration);
                        rank++;
                        resultArr.add(temp);
                    }
                }
                
                JsonObject result = new JsonObject();
                result.addProperty("status", "success");
                result.add("results", resultArr);
                out.println(gson.toJson(result));

            }
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
        processRequest(request, response);
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
