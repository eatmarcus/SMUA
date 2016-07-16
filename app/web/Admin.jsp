<%-- 
    Document   : admin
    Created on : Sep 30, 2015, 5:38:20 PM
    Author     : User
--%>
<%@page import="java.util.Collection"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.ArrayList"%>
<%@include file="protectadmin.jsp" %>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Admin Page</title>
        <!-- Bootstrap Core CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link href="css/simple-sidebar.css" rel="stylesheet">
    </head>
    <body>

        <div id="wrapper">

            <!-- Sidebar -->
            <div id="sidebar-wrapper">
                <ul class="sidebar-nav">
                    <li class="sidebar-brand">
                        Welcome!
                    <li class="img">
                        <img src="img/download.jpg" class="img-circle" width="100" height="100" >
                    </li>
                    <li class ="text">
                        <a href= "Admin.jsp">Home</a>
                    </li>
                    <li class ="text">
                        <a href= "logout.jsp">Logout</a>
                    </li>
                </ul>
            </div>
            <!-- /#sidebar-wrapper -->

            <!-- Page Content -->
            <div id="page-content-wrapper">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-lg-12">
                            <h1>Welcome to Admin Page!</h1>

                            <form action="BootstrapServlet" method="post" enctype="multipart/form-data">
                                <div class="form-group">
                                    <label for="bootstrapFile">Upload File</label>
                                    <input type="file" name="bootstrap-file">
                                    <p class="help-block">Select a file you wish to bootstrap.</p>
                                </div>
                                <button type="submit" class="btn btn-default btn-lg" value ="Bootstrap">Bootstrap</button>

                                <%--<input type='text' name='token' value='eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDk3MTIxNTMsImlhdCI6MTQwOTcwODU1M30.h66rOPHh992gpEPtErfqBP3Hrfkh_nNxYwPG0gcAuCc' /> --%>
                            </form>
                            
                            <p/>
                            <form action="AdditionalFileServlet" method="post" enctype="multipart/form-data">
                                <div class="form-group">
                                    <label for="additionalFile">Upload Additional File</label>
                                    <input type="file" name="additional-file">
                                    <p class="help-block">Select a file you wish to bootstrap.</p>
                                </div>
                                <button type="submit" class="btn btn-default btn-lg" value ="Upload Additional Files">Bootstrap Additional File</button>

                                <%--<input type='text' name='token' value='eyJhbGciOiJIUzI1NiJ9.eyJleHAiOjE0MDk3MTIxNTMsImlhdCI6MTQwOTcwODU1M30.h66rOPHh992gpEPtErfqBP3Hrfkh_nNxYwPG0gcAuCc' /> --%>
                            </form>
                            <p/>
                            
                            <%                                if (request.getAttribute("success") != null) {
                                   // out.println("<div class='load' style='width:100px;height:100px'>");
                                    TreeMap<String, Integer> success = (TreeMap<String, Integer>) request.getAttribute("success");
                                    Iterator<String> iter = success.keySet().iterator();
                                    out.println("<table class='table table-bordered'");
                                    out.println("<tr><th>File</th>");
                                    out.println("<th>Rows Added</th></tr>");
                                    while (iter.hasNext()) {
                                        String file = iter.next();
                                        out.println("<tr><td>");
                                        out.println(file + "</td>");
                                        out.println("<td>" + success.get(file) + "</td></tr>");
                                    }
                                    out.println("</table>");
                                    //out.println("</div>");
                                }

                                if (request.getAttribute("errors") != null) {
                                    ArrayList<TreeMap<Integer, String>> errors = (ArrayList<TreeMap<Integer, String>>) request.getAttribute("errors");
                                    for (int i = 0; i < errors.size(); i++) {
                                        TreeMap<Integer, String> errorMap = errors.get(i);
                                        Iterator<Integer> iter = errorMap.keySet().iterator();

                                        if (errorMap.get(0) == "demographics.csv" && errorMap.size() != 1) {
                                            out.println("<div class='alert alert-warning' role='alert'>");
                                            out.println("There are " + (errorMap.size() - 1) + " errors.");
                                            out.println("</div></br></p>");
                                            out.println("These are the errors for demographics.csv file: ");
                                            out.println("</br>");
                                        } else if (errorMap.get(0) == "app.csv" && errorMap.size() != 1) {
                                            out.println("<div class='alert alert-warning' role='alert'>");
                                            out.println("There are " + (errorMap.size() - 1) + " errors.");
                                            out.println("</div></br></p>");
                                            out.println("These are the errors for app.csv file: ");
                                            out.println("</br>");
                                        } else if (errorMap.get(0) == "app-lookup.csv" && errorMap.size() != 1) {
                                            out.println("<div class='alert alert-warning' role='alert'>");
                                            out.println("There are " + (errorMap.size() - 1) + " errors.");
                                            out.println("</div></br></p>");
                                            out.println("These are the errors for app-lookup.csv file: ");
                                            out.println("</br>");
                                        }
                                        while (iter.hasNext()) {
                                            int key = iter.next();
                                            if (key != 0) {
                                                out.println("Line: " + key + " " + errorMap.get(key));
                                                out.println("</br>");
                                            }
                                        }
                                    }
                                }
                                // Displays if the files uploaded are not .zip
                                if (request.getAttribute("errorMsg") != null) {
                                    out.println("<div class='alert alert-danger' role='alert'><strong>");
                                    out.println("<span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span><span class='sr-only'></span>");
                                    out.println(request.getAttribute("errorMsg"));
                                    out.println("</string></div>");
                                }
                            %>
                        </div>
                    </div>
                </div>
            </div>
            <!-- /#page-content-wrapper -->

        </div>
        <!-- /#wrapper -->

        <!-- jQuery -->
        <script src="js/jquery.js"></script>

        <!-- Bootstrap Core JavaScript -->
        <script src="js/bootstrap.min.js"></script>
    </body>

</html>
