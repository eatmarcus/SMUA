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
import is203.JWTUtility;
import is203.dao.DemographicsDAO;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.catalina.User;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "authenticate", urlPatterns = {"/json/authenticate"})
public class authenticate extends HttpServlet {
    

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
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject results = new JsonObject();
            HttpSession session = request.getSession();
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            JsonArray errorArr = new JsonArray();
            ArrayList<String> errorList = new ArrayList<String>();
            String error = "";
            if (username == null || password == null) {
                error = "invalid username/password";
                errorList.add(error);
            } else if (username.trim().isEmpty() || password.trim().isEmpty()) {
                error = "invalid username/password";
                errorList.add(error);
            } else {
                DemographicsDAO dDAO = new DemographicsDAO();
                HashMap<String, String> detailsMap = dDAO.retrieveAllUserPW();
                if ((username.equals("admin") && password.equals("rajeshlovesg1t7")) || (detailsMap.get(username) != null && detailsMap.get(username).equals(password))) {
                    String sharedSecret = "2468101214161820";
                    String token = JWTUtility.sign(sharedSecret, username);
                    
                    results.addProperty("status", "success");
                    results.addProperty("token", token);
                    
                    out.println(gson.toJson(results));
                    return;
                } else {
                    error = "invalid username/password";
                    errorList.add(error);
                }
        }
        if (errorList.size() != 0) {
            for(String s: errorList){
                JsonPrimitive element = new JsonPrimitive(s);
                errorArr.add(element);
            }
            results.addProperty("status", "error");
// check if this is the correct format
            results.add("messages", errorArr);
            out.println(gson.toJson(results));

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
