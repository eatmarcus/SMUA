
<%@page import="java.util.Iterator"%>
<%@page import="java.util.Set"%>
<%@page import="java.util.LinkedHashMap"%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Top K App Report</title>
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
                                <li><a href="topKAppUsageForm.jsp">Top K App Form</a></li>
                                <li class="active">Report</li>
                            </ol>
                            <h1>Top K App Report</h1>
                            <%            String msg = (String) request.getAttribute("noResults");
                                if (msg != null) {
                                    out.println(msg);
                                }
                            %>

                            <%
                                LinkedHashMap<String, Long> resultsTopApp = (LinkedHashMap<String, Long>) request.getAttribute("resultSet");
                                if (resultsTopApp != null) {
                                    //NavigableSet<Integer> navig = resultsTopApp.descendingKeySet();
                                    out.println("<h2>Results of Top " + request.getParameter("numOfReports") + " most used apps given school</h2>");
                                    out.println("<table class='table table-bordered' >");
                                    out.println("<tr><th> Rank</th><th>App Names</th><th>Duration</th>");
                                    //Iterator<Integer> iter = navig.iterator();
                                    Set<String> appNameSet = resultsTopApp.keySet();
                                    Iterator<String> iter = appNameSet.iterator();
                                    int rank = 1;
                                    String names = "";
                                    long duration = 0;
                                    while (iter.hasNext()) {
                                        names = iter.next();
                                        duration = resultsTopApp.get(names);

                                        //int key = iter.next();
                                        //names = resultsTopApp.get(key);
                                        out.println("<tr>");
                                        out.println("<td>" + rank + "</td><td>" + names + "</td>" + "<td>" + duration + "</td>");
                                        out.println("</tr>");
                                        rank++;
                                        if (names.contains(",")) {
                                            String[] nameArr = names.split(",");
                                            int number = nameArr.length;
                                            rank += number - 1;
                                        }
                                    }
                                    out.println("</table>");
                                }
                            %>

                            <%
                                LinkedHashMap<String, Long> topKStudents = (LinkedHashMap<String, Long>) request.getAttribute("processStudentResults");

                                if (topKStudents != null) {
                                    //NavigableSet<Integer> navig = resultsTopApp.descendingKeySet();
									out.println("<h2>Results of Top " + request.getParameter("numOfReports") + " Students given app</h2>");                                    out.println("<table border = '2'>");
                                    out.println("<tr><th> Rank</th><th>Mac-Address</th><th>Name</th><th>Duration</th>");
                                    //Iterator<Integer> iter = navig.iterator();
                                    Set<String> appNameSet = topKStudents.keySet();
                                    Iterator<String> iter = appNameSet.iterator();
                                    int rank = 1;
                                    String nameWithMacAdd = "";
                                    String macAdd = "";
                                    long duration = 0;
                                    String name = "";
                                    while (iter.hasNext()) {
                                        nameWithMacAdd = iter.next();
                                        duration = topKStudents.get(nameWithMacAdd);
                                        String[] nameArr = nameWithMacAdd.split(",");
                                        if (nameArr.length == 2) {
                                            name = nameArr[1];
                                            macAdd = nameArr[0];
                                        } else {
                                            for (int i = 0; i < nameArr.length; i++) {
                                                if (i % 2 != 0) {
                                                    name += nameArr[i] + ", ";
                                                } else {
                                                    macAdd += nameArr[i] + ", ";
                                                }
                                            }
                                        }
                                        out.println("<tr>");
                                        out.println("<td>" + rank + "</td><td>" + macAdd + "</td><td>" + name + "</td><td>" + duration + "</td>");
                                        out.println("</tr>");
                                        rank++;
                                        if (nameArr.length > 2) {
                                            rank++;
                                        }
                                        duration = 0;
                                        name = "";
                                        macAdd="";
                                    }
                                    out.println("</table>");
                                }
                                
                            %>

                            <%
                                LinkedHashMap<String, Long> processSchoolGivenSchool = (LinkedHashMap<String, Long>) request.getAttribute("processSchoolResults");
                                if (processSchoolGivenSchool != null) {
                                    //NavigableSet<Integer> navig = resultsTopApp.descendingKeySet();
                                    out.println("<h2>Results of Top " + request.getParameter("numOfReports") + " Schools given category</h2>");
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th> Rank</th><th>School</th><th>Duration</th>");
                                    //Iterator<Integer> iter = navig.iterator();
                                    Set<String> schoolNameSet = processSchoolGivenSchool.keySet();
                                    Iterator<String> iter = schoolNameSet.iterator();
                                    int rank = 1;
                                    String names = "";
                                    long duration = 0;
                                    while (iter.hasNext()) {
                                        names = iter.next();
                                        duration = processSchoolGivenSchool.get(names);

                                        //int key = iter.next();
                                        //names = resultsTopApp.get(key);
                                        out.println("<tr>");
                                        out.println("<td>" + rank + "</td><td>" + names + "</td>" + "<td>" + duration + "</td>");
                                        out.println("</tr>");
                                        rank++;
                                        if (names.contains(",")) {
                                            rank++;
                                        }
                                    }
                                    out.println("</table>");
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
