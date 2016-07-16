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
import is203.JWTException;
import is203.JWTUtility;
import is203.controller.breakdownbyapp;
import is203.controller.breakdownbytimedemoservlet;
import is203.dao.breakdownDAO;
import java.io.IOException;
import java.io.PrintWriter;
import static java.lang.System.out;
import java.sql.SQLException;
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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "BasicAppUsageServlet", urlPatterns = {"/json/basic-usetime-report"})
public class BasicAppUsageServlet extends HttpServlet {

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
            throws ServletException, IOException, ParseException, JWTException, SQLException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {
            ArrayList<String> errorList = new ArrayList<String>();
            JsonArray errorArr = new JsonArray();
            String currentToken = null;
            JsonObject problems = new JsonObject();
            HttpSession session = request.getSession();
            String startDate = request.getParameter("startdate");
            String endDate = request.getParameter("enddate");
            String error = "";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (request.getHeader("referer") == null) {
                currentToken = (String) request.getParameter("token");
            } else {
                currentToken = (String) session.getAttribute("token");
            }
            if (currentToken.equals("") || currentToken.isEmpty()) {
                errorList.add("blank token");
            } else {
                try {
                    JWTUtility.verify(currentToken, sharedSecretManager.getSharedSecret());

                } catch (JWTException e) {
                    errorList.add("invalid token");
                }
            }
            if (endDate.equals("") || endDate.isEmpty()) {
                errorList.add("blank enddate");
            }
            if (startDate.equals("") || startDate.isEmpty()) {
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
            if (errorList.size() != 0) {
                for (String s : errorList) {
                    //errorArr.add(s);
                }
                problems.add("breakdown", errorArr);
                out.println(gson.toJson(problems));
            } else {
                JsonObject ifHaveOutput = new JsonObject();
                JsonObject intense = new JsonObject();
                JsonObject normal = new JsonObject();
                JsonObject mild = new JsonObject();
                JsonArray outputArr = new JsonArray();
                breakdownbytimedemoservlet bServlet = new breakdownbytimedemoservlet();
                String[] breakdownTime = bServlet.processBreakDownByTime(date1, date2, request, response);
                intense.addProperty("intense-count", Integer.parseInt(breakdownTime[0]));
                intense.addProperty("intense-percent", Integer.parseInt(breakdownTime[1]));
                normal.addProperty("normal-count", Integer.parseInt(breakdownTime[2]));
                normal.addProperty("normal-percent", Integer.parseInt(breakdownTime[3]));
                mild.addProperty("mild-count", Integer.parseInt(breakdownTime[4]));
                mild.addProperty("mild-percent", Integer.parseInt(breakdownTime[5]));
                outputArr.add(intense);
                outputArr.add(normal);
                outputArr.add(mild);
                ifHaveOutput.addProperty("status", "success");
                ifHaveOutput.add("breakdown", outputArr);

                out.println(gson.toJson(ifHaveOutput));
            }
        }

        PrintWriter out = response.getWriter();

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
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(BasicAppUsageServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
