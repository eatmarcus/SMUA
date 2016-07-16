/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.controller;

import com.opencsv.CSVReader;
import is203.dao.AppDAO;
import is203.dao.ConnectionManager;
import is203.dao.DemographicsDAO;
import is203.entity.AdditionalFile;
import is203.entity.Bootstrap;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;

@WebServlet(name = "AdditionalFileServlet", urlPatterns = {"/AdditionalFileServlet"})
public class AdditionalFileServlet extends HttpServlet {

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
            out.println("<title>Servlet additionalFileServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet additionalFileServlet at " + request.getContextPath() + "</h1>");
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
        ServletFileUpload upload = new ServletFileUpload();
        AdditionalFile af = new AdditionalFile();
        FileItemStream fileStream = null;

        try {
            // Retrieves the upload
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
                    
                    // Gets all the successfully entered rows
                    request.setAttribute("success", success);
                    
                    // Stores errors inside the attribute "errors".
                    request.setAttribute("errors", errors);

                    // Forwards the page back to the admin home page.
                    RequestDispatcher view = request.getRequestDispatcher("Admin.jsp");
                    view.forward(request, response);
                    
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                request.setAttribute("errorMsg", "The file uploaded is not a zip file.");
                RequestDispatcher view = request.getRequestDispatcher("Admin.jsp");
                view.forward(request, response);
            }
        } catch (Exception e) {
         e.printStackTrace();
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Additional File Servlet";
    }// </editor-fold>
    
    /**
     * Merge errors from SQL and file validation together
     * @param merge errors from SQL
     * @param errors errors from file validation
     * @return a TreeMap that contains both errors from SQL and file validation
     */
    public TreeMap<Integer, String> mergeErrors(TreeMap<Integer, String> merge, TreeMap<Integer, String> errors) {
        TreeMap<Integer, String> temp = new TreeMap<Integer, String>();
        temp.putAll(merge);
        temp.putAll(errors);
        errors = temp;
        return errors;
    }
}
