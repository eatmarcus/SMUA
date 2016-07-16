package is203.controller;

import is203.dao.*;
import is203.entity.Bootstrap;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.*;

@WebServlet(name = "BootstrapServlet", urlPatterns = {"/BootstrapServlet"})
public class BootstrapServlet extends HttpServlet {

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
            out.println("<title>Servlet BootstrapServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet BootstrapServlet at " + request.getContextPath() + "</h1>");
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
        
        // Creates new file upload handler
        ServletFileUpload upload = new ServletFileUpload();
        Bootstrap bs = new Bootstrap();
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
                bs.unzipFiles(fileName, fileStream);

                // Retrieves list of validated rows for entry into Demographics table.
                ArrayList<String[]> demo = bs.getDemo();

                // Retrieves list of validated rows for entry into App-lookup table.
                ArrayList<String[]> appLookUp = bs.getAppLookUp();

                Connection conn = null;
                PreparedStatement stmt = null;

                try{
                    // Starts new connection.
                    conn = ConnectionManager.getConnection();

                    // Clears all the data inside the tables.
                    bs.resetTable();

                    // Adds the validated data to the database.
                    long addStartTime= System.currentTimeMillis();
                    
                    DemographicsDAO.add(demo);
                    AppLookUpDAO.add(appLookUp);

                    bs.handleAppAddition();
                    
                    long addEndTime = System.currentTimeMillis();
                    long addSeconds = (addEndTime - addStartTime)/1000;

                    // Retrieves all errors.
                    ArrayList<TreeMap<Integer, String>> errors = bs.getAllErrors();
                    
                    // Retrieves the number of successful rows.
                    TreeMap<String, Integer> success = bs.getAllSuccess();
                    
                    // Stores errors inside the attribute "errors".
                    request.setAttribute("errors", errors);
                    
                    // Stores success inside the attribute "success"
                    request.setAttribute("success", success);
                    
                    // Forwards the page back to the admin home page.
                    RequestDispatcher view = request.getRequestDispatcher("Admin.jsp");
                    view.forward(request, response);
                }catch(SQLException e){
                    e.printStackTrace();
                }finally{
                    ConnectionManager.close(conn,stmt,null);
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
        return "Bootstrap Servlet";
    }// </editor-fold>

}
