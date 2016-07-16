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
@WebServlet(name = "topKSchoolsMostUsedAppServlet", urlPatterns = {"/json/top-k-most-used-schools"})
public class topKSchoolsMostUsedAppServlet extends HttpServlet {

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

            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();
            String startDate = request.getParameter("startdate");

            String endDate = request.getParameter("enddate");
            String kString = request.getParameter("k");
            String appCat = request.getParameter("appcategory");
            String toCompareApp = "";
            if (appCat != null) {

                toCompareApp = appCat.toLowerCase();
            }
           
            int kValue;
            String error = "";
            String token = request.getParameter("token");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (token == null) {
                errorList.add("missing token");
            } else if (token.equals("") || token.isEmpty()) {
                errorList.add("blank token");
            } else {
                try {
                    JWTUtility.verify(token, sharedSecretManager.getSharedSecret());
                    token = (String) session.getAttribute("token");
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
            if (appCat == null) {
                errorList.add("missing app category");
            } else if (!toCompareApp.equals("books") && !toCompareApp.equals("social") && !toCompareApp.equals("education") && !toCompareApp.equals("information") && !toCompareApp.equals("library") && !toCompareApp.equals("local") && !toCompareApp.equals("tools") && !toCompareApp.equals("fitness") && !toCompareApp.equals("games")) {
                errorList.add("invalid app category");
            } else if (appCat.length() == 0) {
                errorList.add("blank app category");
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
                JsonArray resultArr = new JsonArray();
                JsonObject result = new JsonObject();
                processTopKAppServlet pServlet = new processTopKAppServlet();
                LinkedHashMap<String, Long> toReturn = pServlet.processSchoolGivenCat(Integer.parseInt(kString), date1, date2, appCat);
                Set<String> schoolList = toReturn.keySet();
                Iterator<String> schoolIter = schoolList.iterator();
                String currentSchoolName = schoolIter.next();
                Long currentDuration = toReturn.get(currentSchoolName);
                int rank = 1;
                while (schoolIter.hasNext()) {
                    String nextSchoolName = schoolIter.next();
                    JsonObject temp = new JsonObject();
                    Long nextDuration = toReturn.get(nextSchoolName);
                    if (currentDuration == nextDuration) {
                        temp.addProperty("rank", rank);
                        temp.addProperty("name", currentSchoolName);
                        temp.addProperty("duration", currentDuration);
                    } else {
                        temp.addProperty("rank", rank);
                        temp.addProperty("name", currentSchoolName);
                        temp.addProperty("duration", currentDuration);
                        rank++;
                    }
                    resultArr.add(temp);
                    currentSchoolName = nextSchoolName;
                    currentDuration = nextDuration;
                    

                }
                JsonObject temp = new JsonObject();
                temp.addProperty("rank", rank);
                temp.addProperty("school", currentSchoolName);
                temp.addProperty("duration", currentDuration);
                resultArr.add(temp);

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
