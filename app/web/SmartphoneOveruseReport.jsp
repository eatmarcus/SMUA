<%-- 
    Document   : SmartphoneOveruseReport
    Created on : Oct 21, 2015, 6:57:55 PM
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
                                <li><a href="SmartphoneOveruseForm.jsp">Smartphone Overuse Form</a></li>
                                <li class="active">Report</li>
                            </ol>
                            <h1>Smartphone Overuse Report</h1>
                            <%  String[] toPrint = (String[]) request.getAttribute("smartPhoneOveruse");
                                if (toPrint != null) {%>
                                    
                                    <table class="table table-bordered">
                                        <tr>
                                        <th>Overuse Index</th>
                                        <th>Usage Category</th>
                                        <th>Usage Duration</th>
                                        <th>Gaming Category</th>
                                        <th>Gaming Duration</th>
                                        <th>Access Frequency Index</th>
                                        <th>Access Frequency</th>
                                        </tr>
                                        <tr>
                                            <td><%=toPrint[0]%></td>
                                            <td><%=toPrint[1]%></td>
                                            <td><%=toPrint[2]%></td>
                                            <td><%=toPrint[3]%></td>
                                            <td><%=toPrint[4]%></td>
                                            <td><%=toPrint[5]%></td>
                                            <td><%=toPrint[6]%></td>
                                        </tr>
                                    </table>
                                    
                                    <%
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
