
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
public class CRFToSVMNew2 {

    /**
     * @param args the command line arguments
     */

    public static void main(String[] args) {

        boolean ifTrain = false;
        boolean ifLibLinear = false;
        boolean ifInc = false;
        
        String inFile = "";
        String tFile = "";
        String outFile = "";
        String mFile = "";
        String ifTrainS = "";
        String ifLibLinearS = "";
        String ifIncS = "";
        String lMapFile = "";
                
        inFile = args[0];
        tFile = args[1];
        outFile = args[2];
        mFile = args[3];
        ifTrainS = args[4];
        ifLibLinearS = args[5];
        ifIncS = args[6];
        lMapFile = args[7];
        
        if ("1".equals(ifTrainS)) {
            ifTrain = true;
        }
        if ("1".equals(ifIncS)) {
            ifInc = true;
        }
        
        if ("1".equals(ifLibLinearS)) {
            ifLibLinear = true;
        }
        
        TreeMap <String, String> conMap = new TreeMap <String, String>();
        readLabel(lMapFile, conMap);
        
        TreeMap<Integer, HashMap<Integer, String>> inst = new TreeMap<Integer, HashMap<Integer, String>>();
        ArrayList<Integer> sIdx = new ArrayList<Integer>();
        ArrayList<String> tmpl = new ArrayList<String>();
        ArrayList<String> out = new ArrayList<String>();

        HashMap<String, Integer> map = new HashMap<String, Integer>(4000000);
        
        if (!ifTrain || ifInc) {
            readMap(mFile, map);
            System.out.println("read map done");
        }
        
        readTemplate(tFile, tmpl);
        System.out.println("read template done");
        readInst(inFile, inst, sIdx);
        System.out.println("read inst done");
        
        getFeatures(tmpl, inst, sIdx, conMap, out, map, ifTrain, ifLibLinear, ifInc);
        System.out.println("get features done");
        
        writeFile(outFile, out);
        
        /*
        if (ifTrain || ifInc) {
            writeMap(mFile, map);
        }
        */ 
        
    }

    public static void readLabel(String fileName, TreeMap <String, String> map) {

        String str = "";
        {
            BufferedReader txtin = null;
            try {
                txtin = new BufferedReader(new FileReader(fileName));

                while ((str = txtin.readLine()) != null) {
                    String strA[] = str.split("\t", 2);
                    map.put(strA[0], strA[1]);
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

    public static void readMap(String fileName, HashMap<String, Integer> map) {

        String str = "";
        {
            BufferedReader txtin = null;
            try {
                txtin = new BufferedReader(new FileReader(fileName));

                while ((str = txtin.readLine()) != null) {
                    String strA[] = str.split(" ", 2);
                    map.put(strA[1], Integer.parseInt(strA[0]));
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

    public static void readInst(String fileName, TreeMap<Integer, HashMap<Integer, String>> inst, ArrayList<Integer> sIdx) {

        int idx = 1;
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
                        sIdx.add(idx);
                        
                    } else {
                        idx++; // increase sentence number
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
            ArrayList<Integer> sIdx, TreeMap <String, String> conMap,
            ArrayList<String> out, HashMap<String, Integer> map, 
            boolean ifTrain, boolean ifLibLinear, boolean ifInc) {

        for (int i : insts.keySet()) {
            //  7	58	018636330_DH.txt	O
            // -4       -3      -2                      -1
            
            // file name, previous file, next file
            
            if (i  > 0 && i % 10000 == 0) {
                System.out.println(i + " loaded");
            }

            HashMap<Integer, String> inst = insts.get(i);
            String fName = inst.get(inst.size() - 2);
            // current word for print
            String tWord = inst.get(0);
            
            // label
            String tag = inst.get(inst.size() - 1);
            
            if (conMap.containsKey(tag)) {
                tag = conMap.get(tag);
            } else {
                System.out.println(tWord + " " + tag + " " + i);
                tag = "O";
            }
            
            // senetence number
            int eNum = sIdx.get(i);
            
            TreeSet<Integer> sb = new TreeSet<Integer>();
            
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
                
                if (ifTrain || ifInc) {
                    if (!map.containsKey(fStr)) {
                        int mIdx = map.size() + 1;
                        map.put(fStr, mIdx);
                    }
                }
                
                if (map.containsKey(fStr)) {
                    sb.add(map.get(fStr));
                } /*else {
                    System.out.println(fStr + " " + map.get(fStr));
                }*/
            } // for all template
            
            String fvStr = tag + " ";
            if (!ifLibLinear) {
                fvStr += "qid:" + eNum + " ";
            }
            for (int sbt : sb) {
                fvStr += sbt + ":1 ";
            }
            if (!ifLibLinear) {
                fvStr += "#" + tWord;
            }
            out.add(fvStr);
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

