/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package is203.entity;

import com.opencsv.CSVReader;
import is203.dao.AppDAO;
import is203.dao.AppLookUpDAO;
import is203.dao.DemographicsDAO;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.fileupload.FileItemStream;

/**
 *
 * @author Andrea
 */
public class AdditionalFile {

    private final String $reEmail = "^([a-zA-Z0-9.]+)(201[1-5])@(sis|business|law|accountancy|economics|socsc)(.smu.edu.sg)$";
    private final String $reMacAdd = "^[a-fA-F0-9]*$";

    private ArrayList<String[]> demographics;
    private ArrayList<String[]> app;

    private TreeMap<Integer, String> demoErrorMsg;
    private TreeMap<Integer, String> appErrorMsg;

    private HashMap<String, Integer> validatedMacAdd;
    private HashMap<Integer, Integer> validatedAppId;

    private ArrayList<TreeMap<Integer, String>> mainError;

    private TreeMap<Integer, String[]> uploadFileCheck;
    private static TreeMap<String, Integer> originalLines;
    
    public AdditionalFile() {
        this.demographics = new ArrayList<>();
        this.app = new ArrayList<>();

        this.demoErrorMsg = new TreeMap<Integer, String>();
        this.appErrorMsg = new TreeMap<Integer, String>();;

        this.validatedMacAdd = new HashMap<String, Integer>();
        this.validatedAppId = new HashMap<Integer, Integer>();

        this.mainError = new ArrayList<TreeMap<Integer, String>>();

        this.uploadFileCheck = new TreeMap<Integer, String[]>();
        
        this.originalLines = new TreeMap<String, Integer>();

        // Puts in the name of the file that error map is for. Row 0 will never be used as a key during the validation.
        demoErrorMsg.put(0, "demographics.csv");
        appErrorMsg.put(0, "app.csv");

    }
    
    public ArrayList<String[]> getDemo() {
        return demographics;
    }
    
    public TreeMap<Integer, String> getAppError() {
        return appErrorMsg;
    }
    
    public TreeMap<Integer, String[]> getUploadFileCheck() {
        return uploadFileCheck;
    }
    
    public TreeMap<Integer, String> getDemoError() {
        return demoErrorMsg;
    }

    public void unzipFiles(String zipFile, FileItemStream fileStream) throws IOException {
        ZipInputStream zis = null;
        try {
            clearAllLists();
            InputStream stream = fileStream.openStream();

            //get the zip file content
            zis = new ZipInputStream(new BufferedInputStream(stream));

            // Instantiate a new BufferedRead object by taking in an InputStreamReader object. 
            BufferedReader br = new BufferedReader(new InputStreamReader(zis, "UTF-8"));

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

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
                    originalLines.put("Demographics.csv", DemographicsDAO.retrieveTableSize());
                } else if (fileName.equalsIgnoreCase("app.csv")) {
                    while ((line = reader.readNext()) != null) {
                        app.add(line);
                    }
                    originalLines.put("App.csv", AppDAO.retrieveTableSize());
                }
                // Gets the next csv file inside the Zipped file.
                ze = zis.getNextEntry();
            }
            // Loads all the validated mac-address
            validatedMacAdd = DemographicsDAO.retrieveAllMacAdd();
            validatedAppId = AppLookUpDAO.retrieveAllAppId();
            // Sends each list for validation and returns the validated list of data into a List.
            long valiStartTime = System.currentTimeMillis();

            if (!demographics.isEmpty()) {
                demographics = validateDemo(demographics);
                // Adds the three HashMaps of errors into the main error list.
                mainError.add(demoErrorMsg);
            }
            if (!app.isEmpty()) {
                app = validateApp(app);
            }

            long valiEndTime = System.currentTimeMillis();
            long valiSeconds = (valiEndTime - valiStartTime) / 1000;
            System.out.println("Validation timing: " + valiSeconds);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (zis != null) {
                zis.close();
            }
        }
    }

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
                        demoErrorMsg.put(rowCount, "Invalid MacAdd. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "Invalid MacAdd. ";
                        demoErrorMsg.put(rowCount, s);
                    }
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
                        demoErrorMsg.put(rowCount, "CCA record too long. ");
                    } else {
                        String s = demoErrorMsg.get(rowCount);
                        s += "CCA record too long. ";
                        demoErrorMsg.put(rowCount, s);
                    }
                }
                // If there was an error, the row will be removed from the list.
                if (!allValid) {
                    dList.remove(i);
                    i--;
                } else {
                    validatedMacAdd.put(macAdd, 0);
                }
            } else {
                // Enters the row containing blank fields into the HashMap and removes the row from the list.
                demoErrorMsg.put(rowCount, blankField);
                dList.remove(i);
                i--;
            }
            rowCount++;
        }
        return dList;
    }

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
                System.out.println(validatedAppId.size());
                if (validatedAppId.get(checkAppId) == null) {
                    allValid = false;
                    if (appErrorMsg.get(rowCount) == null) {
                        appErrorMsg.put(rowCount, "Invalid App. ");
                    } else {
                        String s = appErrorMsg.get(rowCount);
                        s += "Invalid App. ";
                        appErrorMsg.put(rowCount, s);
                    }
                }

                //validate mac-Add
                String macAdd = str[mac];
                if (macAdd.length() != 40 || !macAdd.matches($reMacAdd)) {
                    allValid = false;
                    if (appErrorMsg.get(rowCount) == null) {
                        appErrorMsg.put(rowCount, "Invalid mac address. ");
                    } else {
                        String s = appErrorMsg.get(rowCount);
                        s += "Invalid mac address. ";
                        appErrorMsg.put(rowCount, s);
                    }
                }

                //Validate matching address
                if (validatedMacAdd.get(macAdd) == null) {
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
            rowCount++;
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

    public void clearAllLists() {
        this.demographics = new ArrayList<>();
        this.app = new ArrayList<>();

        this.demoErrorMsg = new TreeMap<Integer, String>();
        this.appErrorMsg = new TreeMap<Integer, String>();

        this.validatedMacAdd = new HashMap<String, Integer>();
        this.validatedAppId = new HashMap<Integer, Integer>();

        this.mainError = new ArrayList<TreeMap<Integer, String>>();

        this.uploadFileCheck = new TreeMap<Integer, String[]>();

        // Puts in the name of the file that error map is for. Row 0 will never be used as a key during the validation.
        demoErrorMsg.put(0, "demographics.csv");
        appErrorMsg.put(0, "app.csv");
    }
    
    public static TreeMap<String, Integer> getAllSuccess() {
        TreeMap<String, Integer> success = new TreeMap<String, Integer>();
        int demo = DemographicsDAO.retrieveTableSize();
        int app = AppDAO.retrieveTableSize();
        Iterator<String> iter = originalLines.keySet().iterator();
        while (iter.hasNext()) {
            String file = iter.next();
            if (file.equals("Demographics.csv")) {
                int original = originalLines.get(file);
                int change = demo - original;
                success.put(file, change);
            } else {
               int original = originalLines.get(file);
               int change = app - original;
               success.put(file, change); 
            }
        }
        return success;
    }
}
