<%-- 
    Document   : SmartphoneOveruseForm
    Created on : Oct 18, 2015, 1:44:09 PM
    Author     : Andrea Mai
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Smartphone Overuse Report</title>

        <!-- Bootstrap Core CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link href="css/simple-sidebar.css" rel="stylesheet">

        <!-- DatePicker Css -->
        <link href ="css/datepicker.css" rel="stylesheet">
    </head>
    <body>
        <div id="wrapper">
            <%@include file ='studentSideBar.jsp'%>

            <!--Page Content-->

            <!-- Page Content -->
            <div id="page-content-wrapper">
                <div class="container-fluid">
                    <div class="row">
                        <div class="col-lg-12">
                            <ol class="breadcrumb">
                                <li><a href="studentlogin.jsp">Home</a></li>
                                <li class="active">Smartphone Overuse Form</li>
                            </ol>
                            <h1>Smartphone Overuse Report</h1>
                            <link rel="stylesheet" href="//code.jquery.com/ui/1.11.4/themes/smoothness/jquery-ui.css">
                            <script src="//code.jquery.com/jquery-1.10.2.js"></script>
                            <script src="//code.jquery.com/ui/1.11.4/jquery-ui.js"></script>
                            <link rel="stylesheet" href="/resources/demos/style.css">
                            <script>
                                $(function () {
                                    $("#datepicker").datepicker({dateFormat: 'yy-mm-dd'});
                                });
                                $(function () {
                                    $("#datepicker2").datepicker({dateFormat: 'yy-mm-dd'});
                                });
                            </script>
                            <form action='SmartphoneOveruseServlet' method='post'>
                                <table>
                                    <tr><td>Start Date:</td><td><input type="text" id="datepicker" name="startdate">  <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                    <tr><td>End Date:</td><td><input type="text" id="datepicker2" name="enddate">  <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                    <p/><tr><td><button type='submit' class="btn btn-default btn-lg" value='Generate Overuse Report'>Generate Overuse Report</button></td></tr>
                                </table>
                            </form>
                            <%            String errorMsg = (String) request.getAttribute("smartError");
                                if (errorMsg != null) {
                                    out.println("<p/><div class='alert alert-danger' role='alert'>");
                                    out.println("<span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span><span class='sr-only'></span>");
                                    out.println(errorMsg);
                                    out.println("</div>");
                                }
                            %>
                        </div>
                    </div>
                </div>
            </div>
            <!-- /#page-content-wrapper -->

        </div>
        <!-- /#wrapper -->
    </body>
</html>
