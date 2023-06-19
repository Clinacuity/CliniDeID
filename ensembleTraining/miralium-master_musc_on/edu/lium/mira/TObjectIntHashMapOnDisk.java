package edu.lium.mira;
import java.io.*;
import gnu.trove.*;

// uses K.toString() for storing object value
class TObjectIntHashMapOnDisk<K> extends TObjectIntHashMap<K> implements TObjectHashingStrategy<K> {
    final long FREE_ON_DISK = -1L;
    final long REMOVED_ON_DISK = -2L;

    public TObjectIntHashMapOnDisk() {
        super();
    }

    void writeToDisk(String filename) throws IOException {
        RandomAccessFile output = new RandomAccessFile(filename, "rw");
        output.writeInt(_values.length);
        output.writeInt(size());
        for(int i = 0; i < _values.length; i++) {
            output.writeInt(_values[i]);
        }
        long base = output.getFilePointer();
        output.seek(base + _set.length * 16); // make room for locations and lengths
        long locations[] = new long[_set.length];
        long lengths[] = new long[_set.length];
        for(int i = 0; i < _set.length; i++) {
            lengths[i] = 0;
            if(_set[i] == FREE) locations[i] = FREE_ON_DISK;
            else if(_set[i] == REMOVED) locations[i] = REMOVED_ON_DISK;
            else {
                String value = _set[i].toString();
                locations[i] = output.getFilePointer();
                lengths[i] = value.length();
                output.writeChars(value);
            }
        }
        output.seek(base);
        for(int i = 0; i < locations.length; i++) {
            output.writeLong(locations[i]);
            output.writeLong(lengths[i]);
        }
        output.close();
    }

    RandomAccessFile input;
    int diskCapacity;
    int diskSize;

    void loadFromDisk(String filename) throws FileNotFoundException, IOException {
        clear();
        input = new RandomAccessFile(filename, "r");
        diskCapacity = input.readInt();
        diskSize = input.readInt();
        rehash(diskCapacity);
        for(int i = 0; i < _set.length; i++) _set[i] = null; // set to null
        this._hashingStrategy = new ToStringStrategy(); // change hashing strategy to use toString()
        //System.out.println(diskCapacity + " " + diskSize);
    }

    final boolean isMatch(char[] object, long location, long length) throws IOException {
        if(object.length != length) return false;
        final long oldLocation = input.getFilePointer();
        input.seek(location);
        for(int i = 0; i < length; i++) {
            final char read = input.readChar();
            //System.out.println("    " + object[i] + " = " + read);
            if(object[i] != read) {
                //System.out.println("    false");
                input.seek(oldLocation);
                return false;
            }
        }
        input.seek(oldLocation);
        /*if(i >= length) {
            System.out.println("    false2");
            return false;
        }*/
        //System.out.println("    true");
        return true;
    }

    class ToStringStrategy<K> implements TObjectHashingStrategy<K> {
        public final boolean equals(K o1, K o2) {
            return o1 == null ? o2 == null : o1.toString().equals(o2.toString()); //o1 == null ? o2 == null : o1.equals(o2);
        }
        public final int computeHashCode(K o) {
            return o == null ? 0 : o.hashCode();
        }
    }


    protected int index(K obj) {
        final TObjectHashingStrategy<K> hashing_strategy = _hashingStrategy;

        final Object[] set = _set;
        final int length = set.length;
        final int hash = hashing_strategy.computeHashCode(obj) & 0x7fffffff;
        int index = hash % length;
        if(set[index] == null) {
            try {
                input.seek(4 * 2 + diskCapacity * 4 + index * 16);
                long location = input.readLong();
                if(location == FREE_ON_DISK) set[index] = FREE;
                else if(location == REMOVED_ON_DISK) set[index] = REMOVED;
                else {
                    long objectLength = input.readLong();
                    input.seek(location);
                    char object[] = new char[(int) objectLength];
                    for(int i = 0; i < objectLength; i++) object[i] = input.readChar();
                    set[index] = new String(object);
                    input.seek(4 * 2 + index * 4);
                    _values[index] = input.readInt();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return -1;
            }
            //System.out.println(obj.toString() + " " + index + " " + set[index]);
        }
        Object cur = set[index];

        if ( cur == FREE ) return -1;

        // NOTE: here it has to be REMOVED or FULL (some user-given value)
        if ( cur == REMOVED || ! hashing_strategy.equals((K) cur, obj)) {
            // see Knuth, p. 529
            final int probe = 1 + (hash % (length - 2));

            do {
                index -= probe;
                if (index < 0) {
                    index += length;
                }
                if(set[index] == null) {
                    try {
                        input.seek(4 * 2 + diskCapacity * 4 + index * 16);
                        long location = input.readLong();
                        if(location == FREE_ON_DISK) set[index] = FREE;
                        else if(location == REMOVED_ON_DISK) set[index] = REMOVED;
                        else {
                            long objectLength = input.readLong();
                            input.seek(location);
                            char object[] = new char[(int) objectLength];
                            for(int i = 0; i < objectLength; i++) object[i] = input.readChar();
                            set[index] = new String(object);
                            input.seek(4 * 2 + index * 4);
                            _values[index] = input.readInt();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        return -1;
                    }
                    //System.out.println(obj.toString() + " " + index + " " + set[index]);
                }
                cur = set[index];
            } while (cur != FREE
                    && (cur == REMOVED || ! _hashingStrategy.equals((K) cur, obj)));
        }

        return cur == FREE ? -1 : index;
    }

    public int getFromDisk(K key) {
        if(input == null) return super.get(key);
        int hash = _hashingStrategy.computeHashCode(key) & 0x7fffffff;
        int index = hash % diskCapacity;
        try {
            //System.out.println("  " + key.toString() + " index " + index); 
            input.seek(4 * 2 + diskCapacity * 4 + index * 16);
            long location = input.readLong();
            //System.out.println("  location " + location); 
            if(location == FREE_ON_DISK) return 0; // FREE
            long length = input.readLong();
            //System.out.println("  length " + length); 
            char[] object = key.toString().toCharArray();

            if(location == REMOVED_ON_DISK || ! isMatch(object, location, length)) {
                int probe = 1 + (hash % (diskCapacity - 2));
                do {
                    index -= probe;
                    if (index < 0) {
                        index += diskCapacity;
                    }
                    input.seek(4 * 2 + diskCapacity * 4 + index * 16);
                    location = input.readLong();
                    length = input.readLong();
                } while (location != FREE_ON_DISK
                        && (location == REMOVED_ON_DISK || ! isMatch(object, location, length)));
            }

            if(location == FREE_ON_DISK) return 0;
            input.seek(4 * 2 + index * 4);
            int value = input.readInt();
            return value;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void main(String args[]) {
        try {
            TObjectIntHashMapOnDisk<String> hash = new TObjectIntHashMapOnDisk<String>();
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String line;
            int id = 0;
            while(null != (line = input.readLine())) {
                String tokens[] = line.split(" ");
                if(tokens.length > 0) hash.put(tokens[0], id);
                id += 1;
            }
            System.out.println(hash.size());
            hash.writeToDisk("foo.hash");
            String keys[] = hash.keys(new String[0]).clone();
            hash = new TObjectIntHashMapOnDisk<String>();
            hash.loadFromDisk("foo.hash");
            for(String key: keys) {
                int value = hash.get(key);
                System.out.println(key + " " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
