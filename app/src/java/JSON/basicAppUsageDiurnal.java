/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package JSON;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import is203.JWTException;
import is203.JWTUtility;
import is203.controller.diurnalpatternservlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "basicAppUsageDiurnal", urlPatterns = {"/json/basic-diurnalpattern-report"})
public class basicAppUsageDiurnal extends HttpServlet {

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
            throws ServletException, IOException, ParseException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            ArrayList<String> errorList = new ArrayList<String>();
            JsonArray errorArr = new JsonArray();
            String currentToken = null;
            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();
            String date = request.getParameter("date");
            String yearFilter = request.getParameter("yearfilter");
            String gender = request.getParameter("genderfilter");
            String school = request.getParameter("schoolfilter");
            String error = "";

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (request.getHeader("referer") == null) {

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
            if (date == null) {

                errorList.add("missing date");
            } else if (date.equals("") || date.isEmpty()) {
                errorList.add("blank date");
            } else {
                try {
                    date1 = sdf.parse(date);
                } catch (ParseException e) {
                    errorList.add("invalid date");
                }
            }
            if (yearFilter == null) {
                errorList.add("missing year filter");
            } else if (yearFilter.length() == 0) {
                errorList.add("blank year filter");
            } else {
                if (!yearFilter.equals("NA")) {
                    int yearFilterInt = Integer.parseInt(yearFilter);

                    if (yearFilterInt < 2011 || yearFilterInt > 2015) {
                        errorList.add("invalid year filter");
                    }
                }
            }
            if (gender == null) {
                errorList.add("missing gender filter");

            } else if (gender.length() == 0) {
                errorList.add("blank gender filter");
            } else if (gender.length() > 0 && !gender.equals("NA") && (!gender.toLowerCase().equals("m") && !gender.toLowerCase().equals("f"))) {
                errorList.add("invalid gender filter");
            }

            if (school == null) {
                errorList.add("missing school filter");

            } else if (school.length() == 0) {
                errorList.add("blank school filter");
            } else if (school.length() > 0 && !school.equals("NA") && (!school.equals("accountancy") && !school.equals("sis") && !school.equals("sosc") && !school.equals("law") && !school.equals("business") && !school.equals("economics"))) {
                errorList.add("invalid school filter");
            }
            if (errorList.size() != 0) {
                JsonObject temp = new JsonObject();
                for (String s : errorList) {
                    JsonPrimitive element = new JsonPrimitive(s);
                    errorArr.add(element);
                }
                problems.addProperty("status", "error");

                problems.add("messages", errorArr);
                out.println(gson.toJson(problems));

            } else {
                diurnalpatternservlet dps = new diurnalpatternservlet();
                JsonObject result = new JsonObject();
                JsonArray resultArr = new JsonArray();
                ArrayList<String[]> basicAppDiurnal = dps.diurnalResults(date1, yearFilter, gender, school);

                for (int i = 0; i < basicAppDiurnal.size(); i++) {
                    String[] tempArr = basicAppDiurnal.get(i);

                    JsonObject temp = new JsonObject();
                    String upperTiming = tempArr[0];
                    String lowerTiming = tempArr[1];
                    String duration = tempArr[2];
                    int durationInt = Integer.parseInt(duration);
                    
                    String dateRange = upperTiming + "-" + lowerTiming;
                    temp.addProperty("period", dateRange);
                    temp.addProperty("duration", durationInt);
                    resultArr.add(temp);

                }

                result.addProperty("status", "success");
                result.add("breakdown", resultArr);
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
        try {
            processRequest(request, response);

        } catch (ParseException ex) {
            Logger.getLogger(basicAppUsageDiurnal.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
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
        try {
            processRequest(request, response);

        } catch (ParseException ex) {
            Logger.getLogger(basicAppUsageDiurnal.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
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
