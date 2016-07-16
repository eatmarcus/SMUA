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
import is203.dao.AppLookUpDAO;
import is203.dao.ConnectionManager;
import is203.dao.DemographicsDAO;
import is203.entity.Bootstrap;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "bsJSONServlet", urlPatterns = {"/json/bootstrap"})

public class bsJSONServlet extends HttpServlet {

    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

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
            throws ServletException, IOException, FileUploadException, JWTException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            JsonObject objectToReturn = new JsonObject();
            JsonObject isValidToken = null;
            JsonObject demographics = new JsonObject();
            JsonObject app = new JsonObject();
            JsonObject appLook = new JsonObject();
            JsonArray bootArr2 = new JsonArray();
            JsonArray errorArr = new JsonArray();
            ServletFileUpload upload = new ServletFileUpload();

            Bootstrap bs = new Bootstrap();
            FileItemStream fileStream = null;
            ArrayList<TreeMap<Integer, String>> errors = null;
            //else {
            FileItemIterator iter = upload.getItemIterator(request);
            while (iter.hasNext()) {
                System.out.println("no of times");
                fileStream = iter.next();
                InputStream stream = fileStream.openStream();
                if (fileStream.isFormField()) {
                    
                    System.out.println("here");
                    String token = (String) request.getParameter("token");
                    System.out.println(token);
                    isValidToken = checkForValidToken(token);
                    System.out.println(isValidToken);
                    if (isValidToken != null) {
                        //out.println(gson.toJson(isValidToken));
                        return;
                    }
                } else {
                    String fileName = fileStream.getName();
                    String ext = FilenameUtils.getExtension(fileName);

                    if (ext.equals("zip")) {
                        bs.unzipFiles(fileName, fileStream);

                        // Retrieves list of validated rows for entry into Demographics table.
                        ArrayList<String[]> demo = bs.getDemo();

                        // Retrieves list of validated rows for entry into App-lookup table.
                        ArrayList<String[]> appLookUp = bs.getAppLookUp();

                        Connection conn = null;
                        PreparedStatement stmt = null;

                        try {
                            // Starts new connection.

                            conn = ConnectionManager.getConnection();

                            // Clears all the data inside the tables.
                            bs.resetTable();

                            // Adds the validated data to the database.
                            long addStartTime = System.currentTimeMillis();

                            DemographicsDAO.add(demo);
                            AppLookUpDAO.add(appLookUp);

                            bs.handleAppAddition();
                            long addEndTime = System.currentTimeMillis();
                            long addSeconds = (addEndTime - addStartTime) / 1000;
                            System.out.println("Time taken to add to database: " + addSeconds);

                            // Retrieves all errors.
                            errors = bs.getAllErrors();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (errors != null) {
                            JsonArray bootArr = new JsonArray();
                            TreeMap<String, Integer> linesAdd = bs.getAllSuccess();
                            Iterator<String> listIter = linesAdd.keySet().iterator();
                            while(listIter.hasNext()) {
                                JsonObject temp = new JsonObject();
                                String firstFile = listIter.next().toLowerCase();
                                int noLinesAdded = linesAdd.get(firstFile);
                                temp.addProperty(firstFile, noLinesAdded);
                                bootArr.add(temp);
                            }
                            System.out.println(errors.size());
                            demographics = new JsonObject();
                            app = new JsonObject();
                            appLook = new JsonObject();
                            bootArr2 = new JsonArray();
                            errorArr = new JsonArray();
                            for (int i = 0; i < errors.size(); i++) {

                                TreeMap<Integer, String> errorMap = errors.get(i);
                                Iterator<Integer> mapIter = errorMap.keySet().iterator();

                                JsonArray toReturn = new JsonArray();
                                String name = "";
                                if (errorMap.get(i) == "demographics.csv" && errorMap.size() >= 1) {
                                    demographics.addProperty("demographics.csv", errorMap.size() - 1);
                                    name = "demographics.csv";
                                    bootArr2.add(demographics);
                                } else if (errorMap.get(i) == "app.csv" && errorMap.size() >= 1) {
                                    app.addProperty("app.csv", errorMap.size() - 1);
                                    name = "app.csv";
                                    bootArr2.add(app);

                                } else if (errorMap.get(i) == "app-lookup.csv" && errorMap.size() >= 1) {
                                    appLook.addProperty("app-lookup.csv", errorMap.size() - 1);
                                    name = "app-lookup.csv";
                                    bootArr2.add(appLook);
                                }

                                while (mapIter.hasNext()) {
                                    int key = mapIter.next();
                                    if (key != 0) {
                                        JsonObject temp = new JsonObject();
                                        temp.addProperty("file", name);
                                        temp.addProperty("line", key);
                                        temp.addProperty("message", errorMap.get(key));
                                        errorArr.add(temp);
                                    }
                                }
                              
                                objectToReturn.addProperty("status", "success");
                                objectToReturn.add("num-record-loaded", bootArr);
                                if(errorArr.size()!=0){
                                objectToReturn.add("error", errorArr);
                                }
                            }
                        }

                    }
                }
                out.println(gson.toJson(objectToReturn));
            }

        }
    }

    private JsonObject checkForValidToken(String token) {
        JsonObject errorObj = new JsonObject();
        String error = "";
        if (token == null) {

            error = "missing token";
        } else if (token.length() == 0) {
            error = "blank token";
        } else {
            String tokenTest = "";
            try {
                tokenTest = JWTUtility.verify(token, sharedSecretManager.getSharedSecret());

                if (tokenTest == null) {
                    error = "invalid token";
                }
            } catch (JWTException e) {
                error = "invalid token";
            }
        }

        if (error.length() != 0) {
            errorObj.addProperty("status", "error");
            errorObj.addProperty("message", error);

        }
        return errorObj;
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

        } catch (FileUploadException ex) {
            Logger.getLogger(bsJSONServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(bsJSONServlet.class
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

        } catch (FileUploadException ex) {
            Logger.getLogger(bsJSONServlet.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (JWTException ex) {
            Logger.getLogger(bsJSONServlet.class
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
