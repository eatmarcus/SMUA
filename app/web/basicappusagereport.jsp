<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.util.Collections"%>
<%@page import="java.util.Set"%>
<%@page import="java.text.DecimalFormat"%>
<%@page import="is203.dao.DemographicsDAO"%>
<%@page import="java.util.Iterator"%>
<%@page import="java.util.LinkedHashMap"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="java.util.ArrayList"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Basic App Usage Report</title>
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
                                <li><a href="basicappusageform.jsp">Basic App Usage Form</a></li>
                                <li class="active">Report</li>
                            </ol>
                            <h1>Basic App Usage Report</h1>
                            <%            String[] breakDownTime = (String[]) request.getAttribute("result");
                                if (breakDownTime != null) {
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th>Results</th></tr>");
                                    out.println("<tr><td>Intense User: " + breakDownTime[0] + "(" + breakDownTime[1] + "%), Normal User: " + breakDownTime[2] + "(" + breakDownTime[3] + "%), Mild User: " + breakDownTime[4] + "(" + breakDownTime[5] + "%)</td></tr>");
                                    out.println("</table>");
                                }
                                ArrayList<String[]> breakDownAppReport = (ArrayList<String[]>) request.getAttribute("BreakDownAppCat");
                                if (breakDownAppReport != null) { %>
                            <table class='table table-bordered'>
                                <tr>
                                    <th>Category</th>
                                    <th>Seconds</th>
                                    <th>Percentage</th>
                                </tr>
                                <%                  for (String[] s : breakDownAppReport) {%>
                                <tr>
                                    <td><%=s[0]%></td>
                                    <td><%=s[1]%></td>
                                    <td><%=s[2] + "%"%></td>
                                </tr>
                                <%                  } %>  
                            </table>
                            <%          }

                                ArrayList<String[]> diurnalReport = (ArrayList<String[]>) request.getAttribute("diurnalreport");
                                if (diurnalReport != null) { %>
                            <table class='table table-bordered'>
                                <tr>
                                    <th>TimeStamp</th>
                                    <th>Seconds</th>
                                </tr>
                                <% for (int i = 0; i < diurnalReport.size(); i++) {
                                        String[] s = diurnalReport.get(i);
                                        SimpleDateFormat toDay = new SimpleDateFormat("HH:mm");
                                        SimpleDateFormat toHour = new SimpleDateFormat("h a");
                                        Date timeEarly = toDay.parse(s[0]);
                                        Date timeLater = toDay.parse(s[1]);%>
                                <tr>
                                    <td><%= toHour.format(timeEarly) + "~" + toHour.format(timeLater)%></td>
                                    <td><%=s[2]%></td>
                                </tr>
                                <% } %>
                            </table>
                    <%          }
                                
                        LinkedHashMap<String, ArrayList<String[]>> bd1 = (LinkedHashMap<String, ArrayList<String[]>>) request.getAttribute("bauTimeDemoBreakdown1");
                                LinkedHashMap<String, ArrayList<String[]>> bd2 = (LinkedHashMap<String, ArrayList<String[]>>) request.getAttribute("bauTimeDemoBreakdown2");
                                LinkedHashMap<String, ArrayList<String[]>> bd3 = (LinkedHashMap<String, ArrayList<String[]>>) request.getAttribute("bauTimeDemoBreakdown3");
                                LinkedHashMap<String, ArrayList<String[]>> bd4 = (LinkedHashMap<String, ArrayList<String[]>>) request.getAttribute("bauTimeDemoBreakdown4");
                                LinkedHashMap<String, Integer> results = (LinkedHashMap<String, Integer>) request.getAttribute("bauTimeDemoResults");
                                int totalNumOfUsers = 0;

                                if (results != null) {
                                    for (String key : results.keySet()) {
                                        totalNumOfUsers += results.get(key);
                                    }

                                    if (totalNumOfUsers == 0) {
                                        totalNumOfUsers = 1;
                                    }
                                }

                                if (bd4 != null) { 
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th>Results</th></tr>");
                                    for (String key1 : bd1.keySet()) {
                                        String[] keyArr1 = key1.split("[;]");
                                        int currentNum1 = bd1.get(key1).size();
                                        out.println("<tr><td>" + keyArr1[1] + ": " + currentNum1 + " (" + (Math.round((double) currentNum1 / totalNumOfUsers * 100)) + "%)</br>");
                                        for (String key2 : bd2.keySet()) {
                                            if (key2.contains(key1)) {
                                                String[] keyArr2 = key2.split("[;]");
                                                int currentNum2 = bd2.get(key2).size();
                                                out.println("<ul>" + keyArr2[2] + ":" + currentNum2 + " (" + (Math.round((double) currentNum2 / totalNumOfUsers * 100)) + "%)</br>");
                                                for (String key3 : bd3.keySet()) {
                                                    if (key3.contains(key2)) {
                                                        
                                                        String[] keyArr3 = key3.split("[;]");
                                                        int currentNum3 = bd3.get(key3).size();   
                                                        out.println("<ul>" + keyArr3[3] + ":" + currentNum3 + " (" + (Math.round((double) currentNum3 / totalNumOfUsers * 100)) + "%)</br>");
                                                        int count = 0;
                                                        for (String key4 : bd4.keySet()) {
                                                            if (key4.contains(key3)) {
                                                                count++;
                                                                String[] keyArr4 = key4.split("[;]");
                                                                int currentNum4 = bd4.get(key4).size();
                                                                int mild = results.get(key4 + ";Mild");
                                                                int normal = results.get(key4 + ";Normal");
                                                                int intense = results.get(key4 + ";Intense");
                                                                out.println("<ul>" + keyArr4[4] + ":" + currentNum4 + " (" + (Math.round((double) currentNum4 / totalNumOfUsers * 100)) + "%) Intense Users: "
                                                                        + intense + " (" + (Math.round((double) intense / totalNumOfUsers * 100)) + "%), Normal Users: " + normal + " ("
                                                                        + (Math.round((double) normal / totalNumOfUsers * 100)) + "%), Mild Users: " + mild + " (" + (Math.round((double) mild / totalNumOfUsers * 100)) + "%) </ul>");
                                                            }
                                                        }
                                                        if (count > 0) {
                                                            out.println("<br>");
                                                        }
                                                        out.println("</ul>");
                                                    }
                                                }
                                                out.println("</ul>");
                                            }
                                        }
                                        out.println("</td></tr>");
                                    }
                                    out.println("</table>");
                                } else if (bd3 != null) {
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th>Results</th></tr>");
                                    for (String key1 : bd1.keySet()) {
                                        String[] keyArr1 = key1.split("[;]");
                                        int currentNum1 = bd1.get(key1).size();
                                        out.println("<tr><td>" + keyArr1[1] + ": " + currentNum1 + " (" + (Math.round((double) currentNum1 / totalNumOfUsers * 100)) + "%)<br>");
                                        out.println("<br>");

                                        for (String key2 : bd2.keySet()) {
                                            if (key2.contains(key1)) {
                                                String[] keyArr2 = key2.split("[;]");
                                                int currentNum2 = bd2.get(key2).size();
                                                out.println("<ul>" + keyArr2[2] + ": " + currentNum2 + " (" + (Math.round((double) currentNum2 / totalNumOfUsers * 100)) + "%)</b>");
                                                int count = 0;
                                                for (String key3 : bd3.keySet()) {
                                                    if (key3.contains(key2)) {
                                                        count++;
                                                        String[] keyArr3 = key3.split("[;]");
                                                        int currentNum3 = bd3.get(key3).size();
                                                        int mild = results.get(key3 + ";Mild");
                                                        int normal = results.get(key3 + ";Normal");
                                                        int intense = results.get(key3 + ";Intense");
                                                        out.println("<ul>" + keyArr3[3] + ": " + currentNum3 + " (" + (Math.round((double) currentNum3 / totalNumOfUsers * 100))
                                                                + "%) Intense Users: " + intense + " (" + (Math.round((double) intense / totalNumOfUsers * 100))
                                                                + "%), Normal Users: " + normal + " (" + (Math.round((double) normal / totalNumOfUsers * 100))
                                                                + "%), Mild Users: " + mild + " (" + (Math.round((double) mild / totalNumOfUsers * 100)) + "%)</ul>");

                                                    }

                                                }
                                                if (count > 0) {
                                                    out.println("<br>");
                                                }
                                                out.println("</ul>");
                                            }
                                        }
                                        out.println("</td></tr>");
                                    }
                                    out.println("</table");
                                } else if (bd2 != null) {
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th>Results</th></tr>");
                                    for (String key1 : bd1.keySet()) {
                                        String[] keyArr1 = key1.split("[;]");
                                        int currentNum1 = bd1.get(key1).size();
                                        out.println("<tr><td>" + keyArr1[1] + ": " + currentNum1 + " (" + (Math.round((double) currentNum1 / totalNumOfUsers * 100)) + "%)");
                                        for (String key2 : bd2.keySet()) {

                                            if (key2.contains(key1)) {
                                                String[] keyArr2 = key2.split("[;]");
                                                int currentNum2 = bd2.get(key2).size();
                                                int mild = results.get(key2 + ";Mild");
                                                int normal = results.get(key2 + ";Normal");
                                                int intense = results.get(key2 + ";Intense");
                                                out.println("<ul>" + keyArr2[2] + ":" + currentNum2 + " (" + (Math.round((double) currentNum2 / totalNumOfUsers * 100))
                                                        + "%) Intense Users: " + intense + " (" + (Math.round((double) intense / totalNumOfUsers * 100))
                                                        + "%), Normal Users: " + normal + " (" + (Math.round((double) normal / totalNumOfUsers * 100))
                                                        + "%), Mild Users: " + mild + " (" + (Math.round((double) mild / totalNumOfUsers * 100)) + "%)</ul>");
                                            }
                                        }
                                        out.println("</td></tr>");
                                    }
                                    out.println("</table>");
                                } else if (bd1 != null) {
                                    out.println("<table class='table table-bordered'>");
                                    out.println("<tr><th>Results</th></tr>");
                                    for (String key1 : bd1.keySet()) {
                                        String[] keyArr1 = key1.split("[;]");
                                        int currentNum1 = bd1.get(key1).size();
                                        int mild = results.get(key1 + ";Mild");
                                        int normal = results.get(key1 + ";Normal");
                                        int intense = results.get(key1 + ";Intense");
                                        out.println("<tr><td>" + keyArr1[1] + ": " + currentNum1 + " (" + (Math.round((double) currentNum1 / totalNumOfUsers * 100)) + "%), Intense Users: " + intense + " ("
                                                + (Math.round((double) intense / totalNumOfUsers * 100)) + "%), Normal Users: " + normal + " (" + (Math.round((double) normal / totalNumOfUsers * 100))
                                                + "%), Mild Users: " + mild + " (" + (Math.round((double) mild / totalNumOfUsers * 100)) + "%)</td></tr>");
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
