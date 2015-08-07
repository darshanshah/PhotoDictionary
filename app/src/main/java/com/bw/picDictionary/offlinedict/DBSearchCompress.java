package com.bw.picDictionary.offlinedict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DBSearchCompress {
    String INFILE,
            TYPESFILE,
            INFILE_COM,
            INFILE_COM_L1;

    String types[] = new String[100];
    //might not be a good idea, but just for now i'm using two arrays
    ArrayList<String> wordlist = new ArrayList<String>();
    ArrayList<Integer> offsetlist = new ArrayList<Integer>();

    public DBSearchCompress(String path) {
        INFILE = path + File.separator + "wiktionary";
        TYPESFILE = path + File.separator + "Types";
        INFILE_COM = path + File.separator + "primary-index";
        INFILE_COM_L1 = path + File.separator + "secondary-index";
        buildTypesHash();
        fill_word_offset_list_lessIO();
    }

    public void buildTypesHash() {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(TYPESFILE));
            String line = "", token[];
            while ((line = in.readLine()) != null) {
                token = line.split(":");
                types[Integer.parseInt(token[1])] = token[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void fill_word_offset_list_lessIO() {
        BufferedReader in = null;
        RandomAccessFile in1 = null;
        String line;
        int num_bytes = 50 * 1024, offset = 0, right = 0, left = 0, read_bytes = 0, split_pos = 0;
        char buf[] = new char[num_bytes];
        try {
            in1 = new RandomAccessFile(INFILE_COM_L1, "r");
            in1.seek(offset);
            in = new BufferedReader(new FileReader(in1.getFD()));
            while ((read_bytes = in.read(buf, 0, num_bytes)) != -1) {
                left = right = 0;
                for (right = 0; right < read_bytes; ) {
                    if (buf[right] == '\n') {
                        line = new String(buf, left, right - left);
                        split_pos = line.indexOf('#');
                        wordlist.add(line.substring(0, split_pos));
                        offsetlist.add(Integer.parseInt(line.substring(split_pos + 1), 16));
                        right++;
                        left = right;
                        continue;
                    }
                    right++;
                }
                offset += left;
                in1.seek(offset);
                in = new BufferedReader(new FileReader(in1.getFD()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (in1 != null)
                    in1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public int bsearch(String key) {
        //requires that both wordlist and offsetlist are filled and have same size
        int s = 0, e = wordlist.size() - 1, mid;
        while (s < e) {
            mid = (s + e) / 2;
            if (wordlist.get(mid).compareToIgnoreCase(key) > 0)
                e = mid - 1;
            else if (wordlist.get(mid).compareToIgnoreCase(key) < 0)
                s = mid + 1;
            else
                return mid;
        }
        return e;
    }

    public ArrayList<word> search_primary_index(int offset, String key) {
        RandomAccessFile rin = null, rin1 = null;
        BufferedReader in = null, in1 = null;
        ArrayList<word> result = null;
        try {

            rin = new RandomAccessFile(INFILE_COM, "r");
            rin.seek(offset);
            in = new BufferedReader(new FileReader(rin.getFD()));

            int count = 0, split_pos;
            String line, token[];
            ArrayList<Integer> tempoffset = new ArrayList<Integer>();
            word w;

            /************ searching in primary index ***********/
            while (count < 50 && (line = in.readLine()) != null) {
                split_pos = line.indexOf('#');
                if (line.substring(0, split_pos).compareToIgnoreCase(key) == 0)
                    tempoffset.add(Integer.parseInt(line.substring(split_pos + 1), 16));
                count++;
            }

            /*********** searching in main file ***********/
            if (tempoffset.size() > 0) {
                rin1 = new RandomAccessFile(INFILE, "r");
                rin1.seek(tempoffset.get(0));
                in1 = new BufferedReader(new FileReader(rin1.getFD()));
                result = new ArrayList<word>();
                for (int i = 0; i < tempoffset.size(); i++) {
                    line = in1.readLine();
                    w = new word();
                    token = line.split("\t");
                    w.type = Integer.parseInt(token[1]);
                    w.def = token[2];
                    result.add(w);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in1 != null)
                    in1.close();
                if (in != null)
                    in.close();
                if (rin1 != null)
                    rin1.close();
                if (rin != null)
                    rin.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public String[] search(String keyword) //static
    {
        String[] list = null;
        int index = bsearch(keyword);
        if (index > 0)        //in case the word is also there in previous block
        {
            index--;
            ArrayList<word> result = search_primary_index(offsetlist.get(index), keyword); //can be made a class attribute
            //System.out.println("---"+keyword+"---");

            if (result == null) {
                list = null;
                System.out.println("No Result Found");
            } else {
                list = new String[result.size()];
                for (int i = 0; i < result.size(); i++) {
                    //System.out.println((i+1)+") ["+types[result.get(i).type]+"] "+result.get(i).def);
                    list[i] = "[" + types[result.get(i).type] + "]" + result.get(i).def;
                }
            }
        }
        return list;
    }
}

