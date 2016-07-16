/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.dao;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;

public class breakdownDAO {

    /**
     * Given start date and end date, return all mac-address and relevant
     * timestamps that fall between start date & end date
     *
     * @param date1
     * @param date2
     * @return an ArrayList of String [] containing mac-address and timestamp
     */
    public ArrayList<String[]> retrieveWithinStartEnd(Date date1, Date date2) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> toReturn = new ArrayList<String[]>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String stringDate1 = df.format(date1);
        String stringDate2 = df.format(date2);

        try {
            conn = ConnectionManager.getConnection();
            String query = "select mac_address, timestamp from app where (timestamp between '" + stringDate1 + "' and '" + stringDate2 + "') ORDER BY mac_address, timestamp;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String macAdd = rs.getString(1);
                String time = rs.getString(2);
                toReturn.add(new String[]{macAdd, time});
            }

            return toReturn;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return null;
    }

    /**
     * Given start date & end date return all mac-address, timestamps and their
     * demographics Calculate app usage time and category given returned
     * mac-addresses
     *
     * @param date1
     * @param date2
     * @return an ArrayList of String[] containing macAdd, usageCat, school,
     * year, gender, cca
     * @throws ParseException
     */
    public ArrayList<String[]> retrieveWithinTimeDemo(Date date1, Date date2) throws ParseException {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> toReturn = new ArrayList<String[]>();
        String macAdd1 = "";
        String macAdd2 = "";
        String timeStamp1 = "";
        String timeStamp2 = "";
        String email2 = "";
        String gender2 = "";
        String cca2 = "";
        Date dateTime1 = null;
        Date dateTime2 = null;
        long totalTimeOnSP = 0;
        double countIntenseUsers = 0;
        double countNormalUsers = 0;
        double countMildUsers = 0;
        double countUsers = 0;
        String usageCat = "";
        int noOfDays = (int) ((date2.getTime() - date1.getTime()) / (1000 * 60 * 60 * 24) + 1);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String d1 = format.format(date1);
        String d2 = format.format(date2);
        try {
            conn = ConnectionManager.getConnection();
            String query = "select d.mac_address, timestamp, email, gender, cca from demographics d, app a where d.mac_address = a.mac_address and timestamp between '" + d1 + "' and '" + d2 + "' ORDER BY d.mac_address, timestamp";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                macAdd1 = rs.getString(1);
                timeStamp1 = rs.getString(2);
                String email1 = rs.getString(3);
                String gender1 = rs.getString(4);
                String cca1 = rs.getString(5);
                if (macAdd1.equals(macAdd2)) {
                    //calculate duration between the timeStamp1 and timeStamp2
                    dateTime1 = format.parse(timeStamp1);// later time
                    dateTime2 = format.parse(timeStamp2);// earlier time

                    long timeDiff = dateTime1.getTime() - dateTime2.getTime();
                    long timeDiffInSeconds = timeDiff / 1000;

                    if (timeDiffInSeconds > 120) {
                        timeDiffInSeconds = 10;
                    }

                    if (rs.isLast()) {
                        long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                        if (timeToAdd > 10) {
                            timeToAdd = 10;
                        }
                        totalTimeOnSP += timeToAdd;
                        double averageTimeOnSP = totalTimeOnSP / noOfDays;

                        if (averageTimeOnSP >= 18000) {
                            usageCat = "intense";
                        } else if (averageTimeOnSP < 18000 && averageTimeOnSP >= 3600) {
                            usageCat = "normal";
                        } else {
                            usageCat = "mild";
                        }
                        int positionAt = email2.indexOf('@');
                        String year = email2.substring(positionAt - 4, positionAt);
                        String school = email2.substring(positionAt + 1, email2.length() - 11);

                        toReturn.add(new String[]{macAdd2, usageCat, school, year, gender2, cca2});
                    }

                    totalTimeOnSP += timeDiffInSeconds;
                } else if (!macAdd1.equals(macAdd2) && !macAdd2.equals("")) { //diff macAdd
                    long timeToAdd = (date2.getTime() - dateTime2.getTime()) / 1000;
                    if (timeToAdd > 10) {
                        timeToAdd = 10;
                    }
                    totalTimeOnSP += timeToAdd;
                    double averageTimeOnSP = totalTimeOnSP / noOfDays;
                    if (averageTimeOnSP >= 18000) {
                        //intense user
                        usageCat = "intense";
                    } else if (averageTimeOnSP < 18000 && averageTimeOnSP >= 3600) {
                        //normal user
                        usageCat = "normal";
                    } else {
                        //mild user
                        usageCat = "mild";
                    }

                    totalTimeOnSP = 0;
                    int positionAt = email2.indexOf('@');
                    String year = email2.substring(positionAt - 4, positionAt);
                    String school = email2.substring(positionAt + 1, email2.length() - 11);

                    toReturn.add(new String[]{macAdd2, usageCat, school, year, gender2, cca2});
                }
                if (!macAdd1.equals(macAdd2) && rs.isLast()) {
                    long timeToAdd = (date2.getTime() - dateTime1.getTime()) / 1000;
                    if (timeToAdd > 10) {
                        timeToAdd = 10;
                    }
                    totalTimeOnSP += timeToAdd;
                    double averageTimeOnSP = totalTimeOnSP / noOfDays;
                    if (averageTimeOnSP >= 18000) {
                        //intense user
                        usageCat = "intense";
                    } else if (averageTimeOnSP < 18000 && averageTimeOnSP >= 3600) {
                        //normal user
                        usageCat = "normal";
                    } else {
                        //mild user
                        usageCat = "mild";
                    }
                    int positionAt = email1.indexOf('@');
                    String year = email1.substring(positionAt - 4, positionAt);
                    String school = email1.substring(positionAt + 1, email1.length() - 11);

                    toReturn.add(new String[]{macAdd2, usageCat, school, year, gender1, cca1});
                }
                macAdd2 = macAdd1;
                timeStamp2 = timeStamp1;
                email2 = email1;
                gender2 = gender1;
                cca2 = cca1;

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return toReturn;
    }

    /**
     * Given start date & end date return mac-address, timestamp and
     * app-categories of apps used by user
     *
     * @param date1
     * @param date2
     * @return an ArrayList of String[] containing mac-address, timestamp and
     * app category
     */
    public ArrayList<String[]> retrieveWithinStartEndByCategory(Date date1, Date date2) {
        //Param we need: mac-Add, timestamp, category
        //We have demo table, app table, app-lookup table
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> toReturn = new ArrayList<>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate1 = df.format(date1);
        String startDate2 = df.format(date2);
        try {
            conn = ConnectionManager.getConnection();
            //table to get within date only
            String query = "select a1.mac_address, a1.timestamp, a2.app_category from app a1, applookup a2 where a1.app_id = a2.app_id and (timestamp BETWEEN '" + startDate1 + "'and '" + startDate2 + "') ORDER BY mac_address, timestamp, app_category;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String macAdd = rs.getString(1);
                String timeStamp = rs.getString(2);
                String category = rs.getString(3);
                toReturn.add(new String[]{macAdd, timeStamp, category});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return toReturn;
    }

    /**
     * Given start date and end date, retrieve all mac-address and timestamps
     * that falls within start date & end date
     *
     * @param date1
     * @param date2
     * @return and ArrayList of String[] containing mac-address & timestamp
     */
    public ArrayList<String[]> retrieveWithinStartEndDiurnalPattern(Date date1, Date date2) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> toReturn = new ArrayList<String[]>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate1 = df.format(date1);
        String startDate2 = df.format(date2);
        System.out.println(date1);
        System.out.println(date2);
        try {
            conn = ConnectionManager.getConnection();
            String query = "select mac_address, timestamp from app where (timestamp between '" + startDate1 + "' and '" + startDate2 + "') ORDER BY mac_address,timestamp;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String macAdd = rs.getString(1);
                String timeStamp = rs.getString(2);
                toReturn.add(new String[]{macAdd, timeStamp});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return toReturn;
    }

    /**
     * Given start date, end date and demographics return relevant mac-address &
     * timestamp
     *
     * @param date1
     * @param date2
     * @param toSort
     * @return an ArrayLIst of String[] containing mac-address and timestamp
     */
     public ArrayList<String[]> retrieveWithinStartEndDiurnalPatternDemo(Date date1, Date date2, String toSort) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<String[]> toReturn = new ArrayList<String[]>();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate1 = df.format(date1);
        String startDate2 = df.format(date2);
        try {
            conn = ConnectionManager.getConnection();
            String query = "select d.mac_address, timestamp, email, gender from app a, demographics d where a.mac_address = d.mac_address and (timestamp between '" + startDate1 + "' and '" + startDate2 + "') ORDER BY mac_address,timestamp;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String macAdd = rs.getString(1);
                String timeStamp = rs.getString(2);
                String email = rs.getString(3);

                int positionAt = email.indexOf('@');
                String year = email.substring(positionAt - 4, positionAt);
                String school = email.substring(positionAt + 1, email.length() - 11);

                String gender = rs.getString(4).toUpperCase();

                int firstIndex = toSort.indexOf(",");
                int lastIndex = toSort.lastIndexOf(",");
                if (firstIndex == -1) {
                    if (gender.equals(toSort) || year.equals(toSort) || school.equals(toSort)) {
                        toReturn.add(new String[]{macAdd, timeStamp});
                    }
                } else if (firstIndex == lastIndex) {
                    String firstFilter = toSort.substring(0, firstIndex);
                    String secondFilter = toSort.substring(firstIndex + 1, toSort.length());
                    String yearAndSchool = firstFilter + "@" + secondFilter;
                    if (year.equals(firstFilter) && gender.equals(secondFilter) || school.equals(firstFilter) && gender.equals(secondFilter) || email.contains(yearAndSchool)) {
                        toReturn.add(new String[]{macAdd, timeStamp});
                    }
                } else if (firstIndex != lastIndex) {
                    String firstFilter = toSort.substring(0, firstIndex);
                    int secondIndex = toSort.indexOf(",", firstIndex + 1);
                    String secondFilter = toSort.substring(firstIndex + 1, secondIndex);
                    String thirdFilter = toSort.substring(secondIndex + 1, toSort.length());
                    String yearAndSchool = firstFilter + "@" + secondFilter;
                    if (email.contains(yearAndSchool) && gender.equals(thirdFilter)) {
                        toReturn.add(new String[]{macAdd, timeStamp});
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return toReturn;
    }

    /**
     * Given a start date and end date retrieve the number of unique mac-address
     * with timestamps that falls within start date and end date
     *
     * @param dateEarly
     * @param maxTime
     * @return number of users using apps between start date & end date
     */
    public int retrieveNumMacAdd(Date dateEarly, Date maxTime) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String startDate1 = df.format(dateEarly);
        String startDate2 = df.format(maxTime);
        ArrayList<String> numMacAdd = new ArrayList<String>();
        try {
            conn = ConnectionManager.getConnection();
            String query = "select d.mac_address from app a, demographics d where a.mac_address = d.mac_address and (timestamp between '" + startDate1 + "' and '" + startDate2 + "') ORDER BY mac_address,timestamp;";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            while (rs.next()) {
                String macAdd = rs.getString(1);
                if (!numMacAdd.contains(macAdd)) {
                    numMacAdd.add(macAdd);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            ConnectionManager.close(conn, stmt, null);
        }
        return numMacAdd.size();

    }

}
