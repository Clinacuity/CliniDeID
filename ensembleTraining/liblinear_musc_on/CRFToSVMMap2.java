
/*
# © Copyright 2019-2023, Clinacuity Inc. All Rights Reserved.
#
# This file is part of CliniDeID.
# CliniDeID is free software: you can redistribute it and/or modify it under the terms of the
# GNU General Public License as published by the Free Software Foundation,
# either version 3 of the License, or any later version.
# CliniDeID is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
# without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
# PURPOSE. See the GNU General Public License for more details.
# You should have received a copy of the GNU General Public License along with CliniDeID.
# If not, see <https://www.gnu.org/licenses/>.
# =========================================================================   
*/

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author jun
 */
public class CRFToSVMMap2 {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {

        String inFile = "";
        String tFile = "";
        String mFile = "";
        String cFile = "";
        String thS = "";
        
        inFile = args[0];
        tFile = args[1];
        mFile = args[2];
        cFile = args[3];
        thS = args[4];
        
        int th = Integer.parseInt(thS);
        
        TreeMap<Integer, HashMap<Integer, String>> inst = new TreeMap<Integer, HashMap<Integer, String>>();
        ArrayList<String> tmpl = new ArrayList<String>();

        HashMap<String, Integer> map = new HashMap<String, Integer>(4000000);
        HashMap<String, Integer> cnt = new HashMap<String, Integer>(4000000);
        
        readMap(cFile, cnt);
        System.out.println("read map done");
        
        readTemplate(tFile, tmpl);
        System.out.println("read template done");
        readInst(inFile, inst);
        System.out.println("read inst done");
        getFeatures(tmpl, inst, cnt);
        System.out.println("get features done");
        prune(map, cnt, th);
        writeMap(mFile, map);
        writeMap(cFile, cnt);
    }

    public static void prune(HashMap<String, Integer> map, HashMap<String, Integer> cnt, int cutoff) {

        int idx = 1;
        for (String key : cnt.keySet()) {
            int c = cnt.get(key);
            if (c > cutoff) {
                map.put(key, idx);
                idx++;
            }
        }
        
        System.out.println("total: " + map.size() + " out of " + cnt.size() + ". pruned: " + (cnt.size() - map.size()));
    }    
    
    public static void readMap(String fileName, HashMap<String, Integer> map) {

        String str = "";
        {
            BufferedReader txtin = null;
            try {
                txtin = new BufferedReader(new FileReader(fileName));

                while ((str = txtin.readLine()) != null) {
                    if (!str.trim().isEmpty()) {
                        String strA[] = str.split(" ", 2);
                        map.put(strA[1], Integer.parseInt(strA[0]));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    txtin.close();
                } catch (Exception ex) {

               }
            }
        }

    }

    public static void readTemplate(String fileName, ArrayList<String> tmpl) {

        String str = "";
        {
            BufferedReader txtin = null;
            try {
                //U0101:%x[-2,0]/%x[-1,0]
                
                txtin = new BufferedReader(new FileReader(fileName));

                while ((str = txtin.readLine()) != null) {
                    if (!str.trim().isEmpty() && !str.startsWith("#")) {
                        tmpl.add(str.trim());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    txtin.close();
                } catch (Exception ex) {

               }
            }
        }

    }

    public static void readInst(String fileName, TreeMap<Integer, HashMap<Integer, String>> inst) {

        String str = "";
        {
            BufferedReader txtin = null;
            try {
                txtin = new BufferedReader(new FileReader(fileName));

                int iId = 0;
                while ((str = txtin.readLine()) != null) {
                    if (!str.trim().isEmpty()) {
                        
                        String strA[] = str.trim().split("\t");
                        HashMap<Integer, String> strAA = new HashMap<Integer, String>();
                        int fId = 0;
                        for (String tok :  strA) {
                            strAA.put(fId, tok);
                            fId++;
                        }
                        inst.put(iId, strAA);
                        iId++;
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                try {
                    txtin.close();
                } catch (Exception ex) {

               }
            }
        }

    }
    
    public static void getFeatures(ArrayList<String> tmpl, TreeMap<Integer, HashMap<Integer, String>> insts, 
            HashMap<String, Integer> cnt) {

        for (int i : insts.keySet()) {
            //  7	58	018636330_DH.txt	O
            // -4       -3      -2                      -1
            
            if (i  > 0 && i % 10000 == 0) {
                System.out.println(i + " loaded");
            }
            // file name, previous file, next file
            
            HashMap<Integer, String> inst = insts.get(i);
            String fName = inst.get(inst.size() - 2);
            
            // for each feature template
            for (String fs : tmpl) {
                //U0101:%x[-2,0]/%x[-1,0]
                //-2,0/-1,0
                String fsA[] = fs.split(":", 2);
                String fL = fsA[0]; // label
                String fsF = fsA[1].replace("%x", "").replace("[", "").replace("]", "");
                // remove special characters
                        
                String fA[] = fsF.split("/");
                String fStr = ""; // feature str
                for (int k = 0; k < fA.length; k++) {
                    String f = fA[k];
                    
                    String ff[] = f.split(",");
                    int rI = Integer.parseInt(ff[0]) + i;
                    int cI = Integer.parseInt(ff[1]);
                    
                    String tmp = "";
                    
                    String rName = fName; //need to change other files to fName not ""
                    if (rI >= 0 && rI < insts.size()) {
                        rName = insts.get(rI).get(insts.get(rI).size() - 2);
                    }

                    if (rI < 0 || rI >= insts.size() || !fName.equals(rName)) {
                        tmp = "*null*";
                    } else {
                        HashMap<Integer, String> tInst = insts.get(rI);
                        tmp = tInst.get(cI);
                    }
                    if (k == 0) {
                        fStr = tmp;
                    } else {
                        fStr += "/" + tmp;
                    }
                }
                // make each feature
                if (!fStr.contains("*null*")) {
                    fStr = fL + ":" + fStr;
                }
                
                if (!cnt.containsKey(fStr)) {
                    cnt.put(fStr, 1);
                } else {
                    int val = cnt.get(fStr) + 1;
                    cnt.put(fStr, val);
                }
                
            } // for all template
        }// for each word
    }
    
    public static PrintWriter getPrintWriter (String file)
    throws IOException {
        return new PrintWriter (new BufferedWriter
                (new FileWriter(file)));
    }

    public static void writeFile(String name, ArrayList<String> inst) {

        try {

            PrintWriter out = getPrintWriter(name);
            for (String str: inst) {
                out.println(str);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        //System.out.println(idx.size());
    }

    public static void writeMap(String name, HashMap<String, Integer> map) {

        try {

            PrintWriter out = getPrintWriter(name);
            for (String str: map.keySet()) {
                out.println(map.get(str) + " " + str);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        //System.out.println(idx.size());
    }

}

