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
import is203.dao.AppDAO;
import is203.dao.ConnectionManager;
import is203.dao.DemographicsDAO;
import is203.entity.AdditionalFile;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Clifford
 */
@WebServlet(name = "updateAdditionalFilesServlet", urlPatterns = {"/json/update"})
public class updateAdditionalFilesServlet extends HttpServlet {

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
            throws ServletException, IOException, FileUploadException, SQLException {
        response.setContentType("application/JSON");
        try (PrintWriter out = response.getWriter()) {

            ServletFileUpload upload = new ServletFileUpload();
            AdditionalFile af = new AdditionalFile();
            FileItemStream fileStream = null;

            /* TODO output your page here. You may use following sample code. */
            FileItemIterator iter = upload.getItemIterator(request);
            // Gets the zip file 
            fileStream = iter.next();
            // Gets name of zip file
            String fileName = fileStream.getName();
            // Checks that file is in zip format
            String ext = FilenameUtils.getExtension(fileName);

            if (ext.equals("zip")) {
                af.unzipFiles(fileName, fileStream);

                // Retrieves list of validated rows for entry into Demographics table.
                ArrayList<String[]> demo = af.getDemo();

                // Gets errors from specific file validation
                TreeMap<Integer, String> appErrors = af.getAppError();

                // Retrieves map of validated rows for App.
                TreeMap<Integer, String[]> fileCheck = af.getUploadFileCheck();
                Connection conn = null;
                PreparedStatement stmt = null;
                try {
                    // Starts new connection.
                    conn = ConnectionManager.getConnection();

                    // Turns the auto-commit to false.
                    ConnectionManager.disableCommit(conn);

                    ArrayList<TreeMap<Integer, String>> errors = new ArrayList<>();

                    if (!demo.isEmpty()) {
                        DemographicsDAO.add(demo);
                        errors.add(af.getDemoError());
                    }
                    if (!fileCheck.isEmpty()) {
                        // Gets all the duplicate rows with database
                        TreeMap<Integer, String> errorMsg = AppDAO.checkUploadAddDuplicates(fileCheck);

                        // Adds errors to error map
                        appErrors = mergeErrors(errorMsg, appErrors);

                        errors.add(appErrors);
                    }

                    // Retrieves the number of successful rows.
                    TreeMap<String, Integer> success = af.getAllSuccess();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
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
        } catch (FileUploadException ex) {
            Logger.getLogger(updateAdditionalFilesServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(updateAdditionalFilesServlet.class.getName()).log(Level.SEVERE, null, ex);
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
            Logger.getLogger(updateAdditionalFilesServlet.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(updateAdditionalFilesServlet.class.getName()).log(Level.SEVERE, null, ex);
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

    public TreeMap<Integer, String> mergeErrors(TreeMap<Integer, String> merge, TreeMap<Integer, String> errors) {
        TreeMap<Integer, String> temp = new TreeMap<Integer, String>();
        temp.putAll(merge);
        temp.putAll(errors);
        errors = temp;
        return errors;
    }
}
