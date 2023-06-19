package edu.lium.mira;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Vector;

// adapted from "Introduction to Algorithms", by Cormen et al.
public class NBestInt {
    int num;
    int[] objects;
    double[] values;
    int[] next;

    final void swap(int a, int b) {
        final double tmpValue = values[a];
        values[a] = values[b];
        values[b] = tmpValue;
        final int tmpObject = objects[a];
        objects[a] = objects[b];
        objects[b] = tmpObject;
    }
    
    // ------- implementation for n-max

    final void heapifyMin(int index) {
        final int left = index << 1;
        final int right = left + 1;
        int largest = index;
        if(left < num && values[left] < values[index]) largest = left;
        if(right < num && values[right] < values[largest]) largest = right;
        if(largest != index) {
            swap(index, largest);
            heapifyMin(largest);
        }
    }

    final void buildMin() {
        int index = (num - 1) >> 1;
        while(index > 0) {
            heapifyMin(index);
            index >>= 1;
        }
        heapifyMin(index);
    }

    final void extractMin() {
        values[0] = values[num - 1];
        objects[0] = objects[num - 1];
        num --;
        heapifyMin(0);
    }

    final void sortNmax() {
        final int savedNum = num;
        int index = num - 1;
        while(index > 0) {
            swap(0, index);
            num --;
            heapifyMin(0);
            index --;
        }
        num = savedNum;
    }

    public final void insertNmax(double value, int object) {
        if(num >= values.length) {
            if(value < values[0]) return;
            extractMin();
        }
        int index = num;
        values[index] = value;
        objects[index] = object;
        num += 1;
        int parent = index >> 1;
        while(index > 0 && values[parent] > values[index]) {
            swap(index, parent);
            index = parent;
            parent >>= 1;
        }
    }

    // ------- implementation for n-min

    final void heapifyMax(int index) {
        final int left = index << 1;
        final int right = left + 1;
        int largest = index;
        if(left < num && values[left] > values[index]) largest = left;
        if(right < num && values[right] > values[largest]) largest = right;
        if(largest != index) {
            swap(index, largest);
            heapifyMax(largest);
        }
    }

    final void buildMax() {
        int index = (num - 1) >> 1;
        while(index > 0) {
            heapifyMax(index);
            index >>= 1;
        }
        heapifyMax(index);
    }

    final void extractMax() {
        values[0] = values[num - 1];
        objects[0] = objects[num - 1];
        num --;
        heapifyMax(0);
    }

    public final void sortNmin() {
        final int savedNum = num;
        int index = num - 1;
        while(index > 0) {
            swap(0, index);
            num --;
            heapifyMax(0);
            index --;
        }
        num = savedNum;
    }

    public final void insertNmin(double value, int object) {
        if(num >= values.length) {
            if(value > values[0]) return;
            extractMax();
        }
        int index = num;
        values[index] = value;
        objects[index] = object;
        num += 1;
        int parent = index >> 1;
        while(index > 0 && values[parent] < values[index]) {
            swap(index, parent);
            index = parent;
            parent >>= 1;
        }
    }

    public NBestInt(int maxSize) {
        this.num = 0;
        objects = new int[maxSize];
        values = new double[maxSize];
    }

    public int get(int index) {
        return objects[index];
    }

    public double getValue(int index) {
        return values[index];
    }

    public int size() {
        return num;
    }

    public void clear() {
        num = 0;
    }

    public static void main(String args[]) {
        int lineNum = 1;
        String line = "";
        try {
            if(args.length != 2) {
                System.err.println("USAGE: cat <input-file> | java edu.lium.mira.NBestInt <size> <field>");
                System.exit(1);
            }
            NBestInt nbest = new NBestInt(Integer.parseInt(args[0]));
            int field = Integer.parseInt(args[1]);
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            Vector<String> lines = new Vector<String>();
            while(null != (line = reader.readLine())) {
                lines.add(line);
                String tokens[] = line.trim().split("\\s+");
                double value = Double.parseDouble(tokens[field]);
                nbest.insertNmax(value, lineNum - 1);
                lineNum ++;
            }
            nbest.sortNmax();
            for(int i = 0; i < nbest.size(); i++) {
                System.out.println(lines.get(nbest.get(i)));
            }
        } catch (Exception e) {
            System.err.println("ERROR at input line " + lineNum + ": \"" + line + "\"");
            e.printStackTrace();
        }
    }
}
