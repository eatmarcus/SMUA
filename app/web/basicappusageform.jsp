<%-- 
    Document   : basicappusagereport
    Created on : Oct 2, 2015, 12:21:46 PM
    Author     : User
--%>

<%@page import="java.util.ArrayList"%>
<%@page import="java.sql.ResultSet"%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%@ include file = "protectstudent.jsp" %>
<!doctype html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <title>Basic App Usage Report</title>
        <!-- Bootstrap Core CSS -->
        <link href="css/bootstrap.min.css" rel="stylesheet">

        <!-- Custom CSS -->
        <link href="css/simple-sidebar.css" rel="stylesheet">
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
                                <li class="active">Basic App Usage Form</li>
                            </ol>

                            <script type='text/javascript'>
                                function setOptions(chosen, selbox) {
                                    // selbox assignment deleted

                                    selbox.options.length = 0;
                                    if (chosen == "0") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select one of the options above-', '00');
                                        setTimeout(setOptions('00', document.myform.thirdChoice), 5);

                                    }


                                    if (chosen == "year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '00');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'year, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'year, school');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'year, cca');
                                        setTimeout(setOptions('year, gender', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('year, school', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('year, cca', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('00', document.myform.thirdChoice), 5);

                                    }
                                    if (chosen == "gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '00');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'gender, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'gender, school');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'gender, cca');
                                        setTimeout(setOptions('gender, year', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('gender, school', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('gender, cca', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('00', document.myform.thirdChoice), 5);
                                    }
                                    if (chosen == "school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '00');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'school, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'school, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'school, cca');
                                        setTimeout(setOptions('school, year', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('school, gender', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('school, cca', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('00', document.myform.thirdChoice), 5);

                                    }
                                    if (chosen == "cca") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '00');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'cca, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'cca, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'cca, school');
                                        setTimeout(setOptions('cca, year', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('cca, gender', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('cca, school', document.myform.thirdChoice), 5);
                                        setTimeout(setOptions('00', document.myform.thirdChoice), 5);

                                    }


                                    // repeat for entries in first dropdown list
                                    if (chosen == "year, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'year, gender, school');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'year, gender, cca');
                                        setTimeout(setOptions('year, gender, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('year, gender, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "year, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'year, school, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'year, school, cca');
                                        setTimeout(setOptions('year, school, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('year, school, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "year, cca") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'year, cca, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'year, cca, school');
                                        setTimeout(setOptions('year, cca, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('year, cca, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "gender, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'gender, year, school');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'gender, year, cca');
                                        setTimeout(setOptions('gender, year, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('gender, year, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "gender, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'gender, school, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'gender, school, cca');
                                        setTimeout(setOptions('gender, school, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('gender, school, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "gender, cca") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'gender, cca, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'gender, cca, school');
                                        setTimeout(setOptions('gender, cca, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('gender, cca, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "school, year") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'school, year, gender');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'school, year, cca');
                                        setTimeout(setOptions('school, year, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('school, year, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "school, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'school, gender, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'school, year, cca');
                                        setTimeout(setOptions('school, gender, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('school, year, cca', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "school, cca") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'school, cca, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'school, cca, gender');
                                        setTimeout(setOptions('school, cca, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('school, cca, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "cca, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'cca, gender, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'cca, gender, school');
                                        setTimeout(setOptions('cca, gender, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('cca, gender, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "cca, school") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'cca, school, year');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'cca, school, gender');
                                        setTimeout(setOptions('cca, school, year', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('cca, school, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    if (chosen == "cca, year") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'cca, year, school');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'cca, year, gender');
                                        setTimeout(setOptions('cca, year, school', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('cca, year, gender', document.myform.fourthChoice), 5);
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }

                                    if (chosen == "00") {
                                        selbox.options[selbox.options.length] = new
                                                Option('-Select one of the options above-', '000');
                                        setTimeout(setOptions('000', document.myform.fourthChoice), 5);
                                    }
                                    // repeat for all the possible entries in second dropdown list
                                    if (chosen == "year, gender, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'year, gender, school, cca');

                                    }
                                    if (chosen == "year, gender, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'year, gender, cca, school');
                                    }
                                    if (chosen == "year, school, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'year, school, gender, cca');
                                    }
                                    if (chosen == "year, school, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'year, school, cca, gender');
                                    }
                                    if (chosen == "year, cca, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'year, cca, gender, school');
                                    }
                                    if (chosen == "year, cca, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'year, cca, school, gender');
                                    }
                                    if (chosen == "gender, year, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'gender, year, school, cca');
                                    }
                                    if (chosen == "gender, year, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'gender, year, cca, school');
                                    }
                                    if (chosen == "gender, school, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'gender, school, year');
                                    }
                                    if (chosen == "gender, school, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'gender, school, cca, year');
                                    }
                                    if (chosen == "gender, cca, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'gender, cca, year, school');
                                    }
                                    if (chosen == "gender, cca, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'gender, cca, school, year');
                                    }
                                    if (chosen == "school, year, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'school, year, gender, cca');
                                    }
                                    if (chosen == "school, year, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'school, year, cca, gender');
                                    }
                                    if (chosen == "school, gender, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('CCA', 'school, gender, year, cca');
                                    }
                                    if (chosen == "school, gender, cca") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'school, gender, cca, year');
                                    }
                                    if (chosen == "school, cca, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'school, cca, year, gender');
                                    }
                                    if (chosen == "school, cca, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'school, cca, gender, year');
                                    }
                                    if (chosen == "cca, year, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'cca, year, gender, school');
                                    }
                                    if (chosen == "cca, year, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'cca, year, school, gender');
                                    }
                                    if (chosen == "cca, gender, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('School', 'cca, gender, year, school');
                                    }
                                    if (chosen == "cca, gender, school") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'cca, gender, school, year');
                                    }
                                    if (chosen == "cca, school, year") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Gender', 'cca, school, year, gender');
                                    }
                                    if (chosen == "cca, school, gender") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select-', '0000');
                                        selbox.options[selbox.options.length] = new
                                                Option('Year', 'cca, school, gender, year');
                                    }
                                    if (chosen == "000") {

                                        selbox.options[selbox.options.length] = new
                                                Option('-Select one of the options above-', '0000');
                                    }
                                }
                            </script>

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
                                $(function () {
                                    $("#datepicker3").datepicker({dateFormat: 'yy-mm-dd'});
                                });
                                $(function () {
                                    $("#datepicker4").datepicker({dateFormat: 'yy-mm-dd'});
                                });
                                $(function () {
                                    $("#datepicker5").datepicker({dateFormat: 'yy-mm-dd'});
                                });
                            </script>
                            <h1>Basic App Usage Report</h1>
                            <p/>

                            <% String errorMsg = (String) request.getAttribute("errorMsg");
                                if (errorMsg != null) {
                                    out.println("<div class='alert alert-danger' role='alert'>");
                                    out.println("<span class='glyphicon glyphicon-exclamation-sign' aria-hidden='true'></span><span class='sr-only'></span>");
                                    out.println("<strong>" + errorMsg + "</strong>");
                                    out.println("</div>");
                                }
                            %> <p/>
                            <form name ='myform' id="form" action='breakdownbytimedemoservlet' method='post'>

                                <table>
                                    <tr><td>Start Date:</td><td><input type="text" id="datepicker" name="startdate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                    <tr><td>End Date:</td><td><input type="text" id="datepicker2" name="enddate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                </table>
                                <p/>
                            </p>
                                <b><i>Sort By:</i></b></br>
                                <table>

                                    <tr><td>First:</td>
                                        <td><select name="firstChoice" size="1"
                                                    onchange="setOptions(document.myform.firstChoice.options[document.myform.firstChoice.selectedIndex].value, document.myform.secondChoice);">
                                                <option value="0" selected="selected">-Select-</option>
                                                <option value="year">Year</option>
                                                <option value="gender">Gender</option>
                                                <option value="school">School</option>
                                                <option value="cca">CCA</option>
                                            </select></td>
                                    <p /> </tr>

                                <tr><td>Second:</td>
                                    <td><select name="secondChoice" size="1"
                                            onchange="setOptions(document.myform.secondChoice.options[document.myform.secondChoice.selectedIndex].value, document.myform.thirdChoice);">
                                        <option value="0" selected="selected">-Select one of the options above-</option>
                                        </select></td>
                                <p /></tr>

                            <tr><td>Third:</td>
                                <td><select name="thirdChoice" size="1"
                                            onchange="setOptions(document.myform.thirdChoice.options[document.myform.thirdChoice.selectedIndex].value, document.myform.fourthChoice);">
                                        <option value="0" selected="selected">-Select one of the options above-</option>
                                    </select></td>
                            <p /></tr>

                            <tr><td>Fourth:</td>
                                <td><select name="fourthChoice" size="1">
                                        <option value="0" selected="selected">-Select one of the options above-</option>
                                    </select></td></tr>
                                </table><p/><p/>
                                    <button type='submit' class="btn btn-default btn-lg" value='Generate Report'>Generate Report</button>
                            </form> 
                            <p/>
                            <hr style="height:1px;border:none;color:#333;background-color:#333;">
                            <b>Breakdown By App</b>
                                    <form name='appcat' action='breakdownbyapp' method='post'>
                                <table>
                                    <tr><td>Start Date:</td><td><input type="text" id="datepicker3" name="startdate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                    <tr><td>End Date:</td><td><input type="text" id="datepicker4" name="enddate"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td></tr>
                                </table>
                            <p/><p/>
                                <button name='appcat' type='submit' class="btn btn-default btn-lg" value='Generate Report'>Generate Breakdown by App Category</button>
                            </form>
                            <hr style="height:1px;border:none;color:#333;background-color:#333;">
                            <b>Diurnal Pattern</b><p/>
                            <form name ="diurnal" action ="diurnalpatternservlet" method="post">
                                <table>
                                    <tr>
                                        <td>Day: </td>
                                        <td><input type="text" id="datepicker5" name="diurnalpattern"> <span class="glyphicon glyphicon-calendar" aria-hidden="true"></span></td>
                                    </tr>
                                    <tr>
                                        <td>Year: </td>
                                        <td><select name="year"><option value="">---SELECT---</option>
                                                <%for (int i = 2011; i <= 2015; i++) {%>
                                                <option value="<%=i%>"><%=i%></option>
                                                <% } %>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>                
                                        <td>Gender: </td>
                                        <td><select name="gender"><option value="">---SELECT---</option>
                                                <option value="M">Male</option>
                                                <option value="F">Female</option>
                                            </select>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>School: </td>
                                        <td><select name="school"><option value="">---SELECT---</option>
                                                <option value="business">School Of Business</option>
                                                <option value="accountancy">School Of Accountancy</option>
                                                <option value="sis">School Of Information Systems</option>
                                                <option value="economics">School Of Economics</option>
                                                <option value="law">School Of Law</option>
                                                <option value="socsc">School Of Social Science</option>
                                            </select>
                                        </td>
                                    </tr>
                                </table>
                                <p/><p/>
                                <button type='submit' class="btn btn-default btn-lg" value='Generate Report'>Generate Diurnal Pattern</button>
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