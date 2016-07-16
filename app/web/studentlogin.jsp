<%-- 
    Document   : studentlogin
    Created on : Sep 30, 2015, 5:38:28 PM
    Author     : User
--%>
<%@include file="protectstudent.jsp" %> 
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<%--    String name = "";
    if (session.getAttribute("email") != null) {
        name = (String) session.getAttribute("email");
    }
--%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Student Page</title>
        <!-- Bootstrap Core CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link href="css/simple-sidebar.css" rel="stylesheet">
    </head>
    <body>
        <div id="wrapper">
            <%@include file ='studentSideBar.jsp'%>

            <!-- Page Content -->
            <div id="page-content-wrapper">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-lg-12">
                            <h1>Welcome to Student Page!</h1>
                            <p>Welcome to the student page. Please feel free to navigate through the different tabs at the side.</p>
                        </div>
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



