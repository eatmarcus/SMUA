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
import is203.controller.SmartphoneOveruseServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
 * @author Marcus
 */
@WebServlet(name = "SmartPhoneServlet", urlPatterns = {"/json/overuse-report"})
public class SmartPhoneServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final String $reMacAdd = "^[a-fA-F0-9]*$";
    Date startDate1;
    Date endDate1;

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
            response.setContentType("application/JSON");
            ArrayList<String> errorList = new ArrayList<String>();
            JsonArray errorArr = new JsonArray();
            String currentToken = null;
            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();

            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String macAdd = request.getParameter("macaddress");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

            currentToken = (String) request.getParameter("token");
            if (currentToken == null) {
                errorList.add("missing token");
            } else if (currentToken.equals("") || currentToken.isEmpty()) {
                errorList.add("blank token");
            } else {
                try {

                    JWTUtility.verify(currentToken, sharedSecretManager.getSharedSecret());

                } catch (JWTException e) {
                    errorList.add("invalid token");
                }
            }

            if (startDate == null) {

                errorList.add("missing startdate");
            } else if (startDate.equals("") || startDate.isEmpty()) {
                errorList.add("blank startdate");
            } else {
                try {
                    startDate1 = sdf.parse(startDate);
                } catch (ParseException e) {
                    errorList.add("invalid startdate");
                }
            }

            if (endDate == null) {

                errorList.add("missing enddate");
            } else if (endDate.equals("") || endDate.isEmpty()) {
                errorList.add("blank enddate");
            } else {
                try {
                    endDate1 = sdf.parse(endDate);

                } catch (ParseException ex) {
                    errorList.add("invalid enddate");
                }
            }
            if (startDate1 != null && endDate1 != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(endDate1);
                c.add(Calendar.DATE, 1);
                endDate1 = c.getTime();
                long t = endDate1.getTime();
                endDate1 = new Date(t - 1000);
                    // Instance 1: Date 1 after Date 2
                // Instance 2: Date 2 after today's date
                if (startDate1.after(endDate1)) {
                    errorList.add("invalid startdate");
                }
            }

            if (macAdd == null) {
                errorList.add("missing macaddress");
            } else if (macAdd.equals("") || macAdd.isEmpty()) {
                errorList.add("blank macAdd");
            } else {
                if (macAdd.length() != 40 || !macAdd.matches($reMacAdd)) {
                    errorList.add("invalid macaddress");
                }
            }

            if (errorList.size() != 0) {
                problems.addProperty("status", "error");
                for (String s : errorList) {
                    JsonPrimitive element = new JsonPrimitive(s);
                    errorArr.add(element);
                }
                problems.add("messages", errorArr);
                out.println(gson.toJson(problems));
            } else {
                JsonObject usage = new JsonObject();
                JsonObject gaming = new JsonObject();
                JsonObject access = new JsonObject();
                JsonObject results = new JsonObject();
                JsonObject output = new JsonObject();

                JsonArray metrics = new JsonArray();

                SmartphoneOveruseServlet spoServlet = new SmartphoneOveruseServlet();
                String[] smartPhoneOveruse = spoServlet.calculateSPO(macAdd, startDate1, endDate1, request, response);

                String overUseIndex = smartPhoneOveruse[0];
                String usageCat = smartPhoneOveruse[1];
                String usageDuration = smartPhoneOveruse[2];
                String gamingCat = smartPhoneOveruse[3];
                String gamingDuration = smartPhoneOveruse[4];
                String accessCat = smartPhoneOveruse[5];
                String accessFreq = smartPhoneOveruse[6];

                int usageDurationInt = Integer.parseInt(usageDuration);
                int gamingDurationInt = Integer.parseInt(gamingDuration);
                double accessFreqDbl = Double.parseDouble(accessFreq);
                usage.addProperty("usage-category", usageCat);
                usage.addProperty("usage-duration", usageDurationInt);
                gaming.addProperty("gaming-category", gamingCat);
                gaming.addProperty("gaming-duration", gamingDurationInt);
                access.addProperty("accessfrequency-category", accessCat);
                access.addProperty("accessfrequency", accessFreqDbl);

                metrics.add(usage);
                metrics.add(gaming);
                metrics.add(access);

                results.addProperty("overuse-index", overUseIndex);
                results.add("metrics", metrics);

                output.addProperty("status", "success");
                output.add("results", results);
                out.println(gson.toJson(output));
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
