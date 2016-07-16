
<%@page import="java.util.Set"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page import="java.util.NavigableMap"%>
<%@page import="java.util.NavigableSet"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.TreeMap"%>
<%@page import="java.util.ArrayList"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K App Usage Report</title>
        <!-- Bootstrap Core CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link href="css/simple-sidebar.css" rel="stylesheet">

        <!-- DatePicker Css -->
        <link href ="css/datepicker.css" rel="stylesheet">
    </head>

    <%
        ArrayList<String> schools = new ArrayList<String>();
        ArrayList<String> categories = new ArrayList<String>();
        schools.add("Accountancy");
        schools.add("Business");
        schools.add("Economics");
        schools.add("Information Systems");
        schools.add("Law");
        schools.add("Social Science");
        categories.add("Books");
        categories.add("Social");
        categories.add("Education");
        categories.add("Entertainment");
        categories.add("Information");
        categories.add("Library");
        categories.add("Local");
        categories.add("Tools");
        categories.add("Fitness");
        categories.add("Games");
        categories.add("Others");
    %>

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
                                <li class="active">Top K App Usage Form</li>
                            </ol>
                            <h1>Top K App Usage Report</h1>

                            <form action="processTopKAppServlet" method="get">
                                Number of Reports:
                                <select name="numOfReports">
                                    <%                                        for (int i = 1; i <= 10; i++) {
                                            out.println("<option value=" + i + ">" + i + "</option>");
                                        }
                                    %>
                                </select>

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
                                <p/>

                                <table>
                                    <tr><td>Start Date:</td><td><input type="text" id="datepicker" name="startdate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                    <tr><td>End Date:</td><td><input type="text" id="datepicker2" name="enddate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>

                                </table>
                                <%--If there is any error on calendar--%>               
                                <%
                                    if (request.getAttribute("errorForm") != null) {
                                        String err = (String) request.getAttribute("errorForm");
                                        out.println("<font color ='red'>** " + err + "</font>");
                                    }
                                %>
                                <p/>
                                <hr style="height:1px;border:none;color:#333;background-color:#333;">
                                <b> Top-k most used apps (given a school)</b>
                                <p/>
                                <table>
                                    <tr><td><select name="school">
                                                <option selected>School</option>
                                                <option value="accountancy">Accountancy</option>
                                                <option value="business" name="business">Business</option>
                                                <option value="economics">Economics</option>
                                                <option value="sis">Information Systems</option>
                                                <option value="law">Law</option>
                                                <option value="socsc">Social Science</option>

                                            </select></td></tr>
                                </table>
                                <p/>
                                <button name='submit1' type='submit' class="btn btn-default btn-lg" >Generate Top-K Apps</button></p>
                                
                                <hr style="height:1px;border:none;color:#333;background-color:#333;">
                                <b>Top-k students with most app usage (given an app category)</b>
                                <p/>
                                <table>
                                    <tr><td>
                                            <select name="cat1">
                                                <option selected>Category</option>
                                                <%
                                                    for (int i = 0; i < categories.size(); i++) {
                                                        String cat = categories.get(i);
                                                %> 
                                                <option value="<%=cat%>"><%=cat%></option>
                                                <%
                                                    }
                                                %>
                                            </select></td></tr>
                                </table><p/>
                                            <button name='submit2' type='submit' class="btn btn-default btn-lg" >Generate Top-K Students</button></p>
                                <p/>
                                <hr style="height:1px;border:none;color:#333;background-color:#333;">
                                <b>Top-k schools with most app usage (given an app category)</b>
                                <table>
                                    <tr><td>
                                            <select name="cat2">
                                                <option selected>Category</option>
                                                <%
                                                    for (int i = 0; i < categories.size(); i++) {
                                                        String cat = categories.get(i);
                                                %>
                                                <option value="<%=cat%>"><%=cat%></option>
                                                <%
                                                    }
                                                %>
                                            </select></td></tr>
                                </table>
                            <p/>
                                            <button name='submit3' type='submit' class="btn btn-default btn-lg" >Generate Top-K Schools</button></p>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
            <!-- /#page-content-wrapper -->

        </div>
        <!-- /#wrapper -->
    </body>
</html>
