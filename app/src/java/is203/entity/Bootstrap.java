/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */package is203.entity;

import com.opencsv.CSVReader;
import is203.dao.*;
import java.io.*;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.*;
import org.apache.commons.fileupload.FileItemStream;

public class Bootstrap {

    private final String $reEmail = "^([a-zA-Z0-9.]+)(201[1-5])@(sis|business|law|accountancy|economics|socsc)(.smu.edu.sg)$";
    private final String $reMacAdd = "^[a-fA-F0-9]*$";

    private TreeMap<Integer, String> demoErrorMsg;
    private TreeMap<Integer, String> appErrorMsg;
    private TreeMap<Integer, String> appLookUpErrorMsg;

    private ArrayList<String[]> demographics;
    private ArrayList<String[]> app;
    private ArrayList<String[]> appLookUp;

    private HashMap<String, Integer> validatedMacAdd;
    private HashMap<Integer, Integer> validatedAppId;

    private ArrayList<TreeMap<Integer, String>> mainError;

    private TreeMap<Integer, String[]> uploadFileCheck;

    /**
     * Constructs a default bootstrap
     */
    public Bootstrap() {
        this.demographics = new ArrayList<>();
        this.app = new ArrayList<>();
        this.appLookUp = new ArrayList<>();

        this.demoErrorMsg = new TreeMap<Integer, String>();
        this.appErrorMsg = new TreeMap<Integer, String>();
        this.appLookUpErrorMsg = new TreeMap<Integer, String>();

        this.validatedMacAdd = new HashMap<String, Integer>();
        this.validatedAppId = new HashMap<Integer, Integer>();

        this.mainError = new ArrayList<TreeMap<Integer, String>>();

        this.uploadFileCheck = new TreeMap<Integer, String[]>();

        // Puts in the name of the file that error map is for. Row 0 will never be used as a key during the validation.
        demoErrorMsg.put(0, "demographics.csv");
        appErrorMsg.put(0, "app.csv");
        appLookUpErrorMsg.put(0, "app-lookup.csv");
    }

    /**
     * Returns an ArrayList of Student Demographics
     *
     * @return list of student demographics
     */
    public ArrayList<String[]> getDemo() {
        return demographics;
    }

    /**
     * Returns an ArrayList of App Usage History
     *
     * @return list of app usage history
     */
    public ArrayList<String[]> getApp() {
        return app;
    }

    /**
     * Returns an ArrayList of Apps
     *
     * @return list of app usage history
     */
    public ArrayList<String[]> getAppLookUp() {
        return appLookUp;
    }

    public TreeMap<Integer, String> getAppError() {
        return appErrorMsg;
    }

    public TreeMap<Integer, String> getDemoError() {
        return demoErrorMsg;
    }

    public TreeMap<Integer, String[]> getUploadFileCheck() {
        return uploadFileCheck;
    }

    /**
     * Unzips file uploaded by admin, validates all files inside
     *
     * @param zipFile name of zipfile uploaded by Admin
     * @param fileStream stream required to read zipfile
     * @throws IOException if an I/O error occurs
     */
    public void unzipFiles(String zipFile, FileItemStream fileStream) throws IOException {
        ZipInputStream zis = null;
        try {

            InputStream stream = fileStream.openStream();

            //get the zip file content
            zis = new ZipInputStream(new BufferedInputStream(stream));

            // Instantiate a new BufferedRead object by taking in an InputStreamReader object. 
            BufferedReader br = new BufferedReader(new InputStreamReader(zis, "UTF-8"));

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            // Clears any existing data inside the lists
            clearAllLists();

            while (ze != null) {
                // Get file name
                String fileName = ze.getName();

                CSVReader reader = new CSVReader(br, ',');
                String[] line = null;

                // Looks for the correct file name and adds every line in the csv into a List
                if (fileName.equalsIgnoreCase("demographics.csv")) {
                    while ((line = reader.readNext()) != null) {
                        demographics.add(line);
                    }
                } else if (fileName.equalsIgnoreCase("app-lookup.csv")) {
                    while ((line = reader.readNext()) != null) {
                        appLookUp.add(line);
                    }
                } else if (fileName.equalsIgnoreCase("app.csv")) {
                    while ((line = reader.readNext()) != null) {
                        app.add(line);
                    }
                }
                // Gets the next csv file inside the Zipped file.
                ze = zis.getNextEntry();
            }

            // Sends each list for validation and returns the validated list of data into a List.
            long valiStartTime = System.currentTimeMillis();

            if (!demographics.isEmpty()) {
                this.demographics = validateDemo(demographics);
            }
            if (!appLookUp.isEmpty()) {
                this.appLookUp = validateAppLookUp(appLookUp);
            }
            if (!app.isEmpty()) {
                this.app = validateApp(app);
            }

            long valiEndTime = System.currentTimeMillis();
            long valiSeconds = (valiEndTime - valiStartTime) / 1000;
            System.out.println("Validation timing: " + valiSeconds);

            // Adds the three HashMaps of errors into the main error list.
            mainError.add(demoErrorMsg);
            mainError.add(appLookUpErrorMsg);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
    }

    /**
     * Validates values inside demographics.csv
     *
     * @param dList The list of records to be validated
     * @return validated list of records
     */
    public ArrayList<String[]> validateDemo(ArrayList<String[]> dList) {
        String[] headers = dList.get(0);
        int mac = 0;
        int n = 0;
        int gen = 0;
        int e = 0;
        int c = 0;
        int p = 0;
        for (int i = 0; i < headers.length; i++) {
            String column = headers[i];
            switch (column.toLowerCase().trim()) {
                case "mac-address":
                    mac = i;
                    break;
                case "name":
                    n = i;
                    break;
                case "gender":
                    gen = i;
                    break;
                case "email":
                    e = i;
                    break;
                case "password":
                    p = i;
                    break;
                case "cca":
                    c = i;
                    break;
            }
        }
        int rowCount = 2;
        String blankField = "";
        for (int i = 1; i < dList.size(); i++) {
            // Returns false if any field is invalid.
            boolean allValid = true;
            String[] str = dList.get(i);
            // Returns a string of errors if there are blank fields.
            blankField = checkEmptyFields(str, dList);
            if (blankField.isEmpty()) {
                //validate mac-Add
                String macAdd = str[mac];
                if (macAdd.length() != 40 || !macAdd.matches($reMacAdd)) {
                    allValid = false;
                    if (demoErrorMsg.get(rowCount) == null) {
                        demoErrorMsg.put(rowCount, "Invalid MAC Address. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "Invalid MAC Address. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                } else {
                    validatedMacAdd.put(macAdd, 0);
                }

                //Get Name
                String name = str[n];

                //validate password
                String password = str[p];
                if (password.length() < 8 || password.contains(" ")) {
                    allValid = false;
                    if (demoErrorMsg.get(rowCount) == null) {
                        demoErrorMsg.put(rowCount, "Invalid password. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "Invalid password. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                }

                //validate email
                String email = str[e];
                if (!email.matches($reEmail)) {
                    allValid = false;
                    if (demoErrorMsg.get(rowCount) == null) {
                        demoErrorMsg.put(rowCount, "Invalid email. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "Invalid email. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                }

                //validate gender
                String gender = str[gen];
                if (!gender.equals("M") && !gender.equals("m") && !gender.equals("F") && !gender.equals("f")) {
                    allValid = false;
                    if (demoErrorMsg.get(rowCount) == null) {
                        demoErrorMsg.put(rowCount, "Invalid gender. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "Invalid gender. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                }
                //validate cca
                String cca = str[c];
                if (cca.length() > 63) {
                    allValid = false;
                    if (demoErrorMsg.get(rowCount) == null) {
                        demoErrorMsg.put(rowCount, "cca record too long. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "cca record too long. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                }
                // If there was an error, the row will be removed from the list.
                if (!allValid) {
                    dList.remove(i);
                    i--;
                }
            } else {
                // Enters the row containing blank fields into the HashMap and removes the row from the list.
                demoErrorMsg.put(rowCount, blankField);
                dList.remove(i);
                i--;
            }
            ++rowCount;
        }
        return dList;
    }

    /**
     * Validates app.csv
     *
     * @param aList list of records to be validated
     * @return list of validated records
     */
    public ArrayList<String[]> validateApp(ArrayList<String[]> aList) {
        int rowCount = 2;
        String blankField = "";
        ArrayList<String[]> returnList = new ArrayList<String[]>();
        TreeMap<Integer, String[]> toCheckMap = new TreeMap<Integer, String[]>();
        String[] headers = app.get(0);
        uploadFileCheck.put(0, headers);
        int ts = 0;
        int mac = 0;
        int a_id = 0;
        for (int i = 0; i < headers.length; i++) {
            String column = headers[i];
            switch (column.toLowerCase().trim()) {
                case "app-id":
                    a_id = i;
                    break;
                case "timestamp":
                    ts = i;
                    break;
                case "mac-address":
                    mac = i;
                    break;
            }
        }
        for (int i = 1; i < aList.size(); i++) {
            boolean allValid = true;
            String[] str = aList.get(i);
            // Returns a string of errors if there are blank fields.
            blankField = checkEmptyFields(str, aList);
            if (blankField.isEmpty()) {
                //validate app-Id
                boolean appIsValid = false;
                int checkAppId = 0;

                // Catches any NumberFormatException caused by parsing a non-integer.
                try {
                    checkAppId = Integer.parseInt(str[a_id]);
                } catch (NumberFormatException e) {
                    appIsValid = false;
                }

                //Validate appId
                if (validatedAppId.get(checkAppId) == null) {
                    allValid = false;
                    if (appErrorMsg.get(rowCount) == null) {
                        appErrorMsg.put(rowCount, "Invalid app. ");
                    } else {
                        String s = appErrorMsg.get(rowCount);
                        s += "Invalid app. ";
                        appErrorMsg.put(rowCount, s);
                    }
                }

                //validate mac-Add
                boolean validMac = true;
                String macAdd = str[mac];
                if (macAdd.length() != 40 || !macAdd.matches($reMacAdd)) {
                    allValid = false;
                    validMac = false;
                    if (appErrorMsg.get(rowCount) == null) {
                        appErrorMsg.put(rowCount, "Invalid mac address. ");
                    } else {
                        String s = appErrorMsg.get(rowCount);
                        s += "Invalid mac address. ";
                        appErrorMsg.put(rowCount, s);
                    }
                }

                //Validate matching address
                if (validMac && validatedMacAdd.get(macAdd) == null) {
                    allValid = false;
                    if (appErrorMsg.get(rowCount) == null) {
                        appErrorMsg.put(rowCount, "No matching mac address. ");
                    } else {
                        String s = appErrorMsg.get(rowCount);
                        s += "No matching mac address. ";
                        appErrorMsg.put(rowCount, s);
                    }
                }

                //Validate time stamp
                String timeStamp = str[ts].trim();
                Pattern p = Pattern.compile("^\\d{4}[-]?\\d{2}[-]?\\d{2} \\d{2}:\\d{2}:\\d{2}");
                if (p.matcher(timeStamp).matches()) {
                    try {
                        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        date.setLenient(false);
                        Date time = date.parse(timeStamp);
                    } catch (ParseException e) {    
                        allValid = false;
                        if (appErrorMsg.get(rowCount) == null) {
                            appErrorMsg.put(rowCount, "Invalid timestamp. ");
                        } else {
                            String s = appErrorMsg.get(rowCount);
                            s += "Invalid timestamp. ";
                            appErrorMsg.put(rowCount, s);
                        }
                    }
                } else {
                    allValid = false;
                        if (appErrorMsg.get(rowCount) == null) {
                            appErrorMsg.put(rowCount, "Invalid timestamp. ");
                        } else {
                            String s = appErrorMsg.get(rowCount);
                            s += "Invalid timestamp. ";
                            appErrorMsg.put(rowCount, s);
                        }
                }
                if (allValid) {
                    uploadFileCheck.put(rowCount, str);
                }
            } else {
                // Adds the row into the HashMap if there were any blank fields and removes the row from the list.
                appErrorMsg.put(rowCount, blankField);
                aList.remove(i);
                i--;
            }
            ++rowCount;
        }

        // Converts the hashmap of validated non-duplicate rows into ArrayList
        Iterator<Integer> iter = uploadFileCheck.keySet().iterator();
        while (iter.hasNext()) {
            int row = iter.next();
            String[] string = uploadFileCheck.get(row);
            String[] newString = new String[]{row + "", string[0], string[1], string[2]};
            returnList.add(newString);
        }

        return returnList;
    }

    /**
     * Validates app-lookup.csv
     *
     * @param aLookList list of records to be validated
     * @return list of validated records
     */
    public ArrayList<String[]> validateAppLookUp(ArrayList<String[]> aLookList) {
        int rowCount = 2;
        String blankField = "";

        String[] headers = aLookList.get(0);
        int a_id = 0;
        int a_n = 0;
        int a_c = 0;
        for (int i = 0; i < headers.length; i++) {
            String column = headers[i];
            switch (column.toLowerCase().trim()) {
                case "app-id":
                    a_id = i;
                    break;
                case "app-name":
                    a_n = i;
                    break;
                case "app-category":
                    a_c = i;
                    break;
            }
        }
        for (int i = 1; i < aLookList.size(); i++) {
            // Returns false if any field is invalid.
            boolean allValid = true;
            String[] str = aLookList.get(i);
            // Returns a string of errors if there are blank fields.
            blankField = checkEmptyFields(str, aLookList);
            if (blankField.isEmpty()) {
                //validate app_Id
                String appId = str[a_id];
                int app_Id = Integer.parseInt(appId);
                if (app_Id <= 0) {
                    allValid = false;
                    if (appLookUpErrorMsg.get(rowCount) == null) {
                        appLookUpErrorMsg.put(rowCount, "Invalid app-id. ");
                    } else {
                        String s = appLookUpErrorMsg.get(rowCount);
                        s += "Invalid app-id. ";
                        appLookUpErrorMsg.put(rowCount, s);
                    }
                } else {
                    validatedAppId.put(app_Id, 0);
                }

                // Get App Name
                String appName = str[a_n];

                String[] categories = {"Books", "Social", "Education", "Entertainment",
                    "Information", "Library", "Local", "Tools", "Fitness",
                    "Games", "Others"};

                //Validate App Category
                String appCategory = str[a_c];
                boolean containsApp = false;
                for (String s : categories) {
                    if (s.equals(appCategory)) {
                        containsApp = true;
                    }
                }
                if (!containsApp) {
                    allValid = false;
                    if (appLookUpErrorMsg.get(rowCount) == null) {
                        appLookUpErrorMsg.put(rowCount, "Invalid app-category. ");
                    } else {
                        String s = appLookUpErrorMsg.get(rowCount);
                        s += "Invalid app-category. ";
                        appLookUpErrorMsg.put(rowCount, s);
                    }
                }

                // If there were any errors in the row, the line is removed from the list.
                if (!allValid) {
                    aLookList.remove(i);
                    i--;
                }
            } else {
                // Adds the row into the HashMap if there were any blank fields and removes the row from the list.
                appLookUpErrorMsg.put(rowCount, blankField);
                aLookList.remove(i);
                i--;
            }
            ++rowCount;
        }
        return aLookList;
    }

    /**
     * Checks for empty fields in csv
     *
     * @param string Array to be validated
     * @param list list of records in csv
     * @return Return errors if any
     */
    public String checkEmptyFields(String[] string, ArrayList<String[]> list) {
        String errors = "";
        for (int i = 0; i < string.length; i++) {
            String value = string[i].trim();
            if (value.isEmpty()) {
                String field = list.get(0)[i];
                if (errors.isEmpty()) {
                    errors = "Blank " + field + ", ";
                } else {
                    errors += " blank " + field + ", ";
                }
            }
        }
        if (!(errors.isEmpty())) {
            errors = errors.substring(0, (errors.length() - 2));
        }
        return errors;
    }

    /**
     * Clears existing database
     *
     * @param conn
     * @param stmt
     */
    public void resetTable() {
        DemographicsDAO.removeAll();
        AppDAO.removeAll();
        AppLookUpDAO.removeAll();

    }

    public ArrayList<TreeMap<Integer, String>> getAllErrors() {
        mainError.add(appErrorMsg);
        return mainError;
    }

    public void handleAppAddition() {
        AppDAO.insertSn();

        AppDAO.dropPrimaryKey();

        AppDAO.add(app);
        
        appErrorMsg = AppDAO.checkDuplicateRows(appErrorMsg);

        AppDAO.insertPrimaryKey();
        AppDAO.deleteSn();
    }

    public TreeMap<String, Integer> getAllSuccess() {
        TreeMap<String, Integer> success = new TreeMap<String, Integer>();
        int demo = DemographicsDAO.retrieveTableSize();
        int app = AppDAO.retrieveTableSize();
        int applookup = AppLookUpDAO.retrieveTableSize();
        success.put("Demographics.csv", demo);
        success.put("App.csv", app);
        success.put("AppLookUp.csv", applookup);
        return success;
    }

    public void clearAllLists() {
        this.demographics = new ArrayList<>();
        this.app = new ArrayList<>();
        this.appLookUp = new ArrayList<>();

        this.demoErrorMsg = new TreeMap<Integer, String>();
        this.appErrorMsg = new TreeMap<Integer, String>();
        this.appLookUpErrorMsg = new TreeMap<Integer, String>();

        this.validatedMacAdd = new HashMap<String, Integer>();
        this.validatedAppId = new HashMap<Integer, Integer>();

        this.mainError = new ArrayList<TreeMap<Integer, String>>();

        this.uploadFileCheck = new TreeMap<Integer, String[]>();

        // Puts in the name of the file that error map is for. Row 0 will never be used as a key during the validation.
        demoErrorMsg.put(0, "demographics.csv");
        appErrorMsg.put(0, "app.csv");
        appLookUpErrorMsg.put(0, "app-lookup.csv");
    }

}
