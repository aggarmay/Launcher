package fr.orange.utils;
import java.io.*;
import java.util.*;


public class LinkedProperties extends Properties {
    private final HashSet<Object> keys = new LinkedHashSet<Object>();
    public LinkedProperties(){

    }
    public Iterable<Object> orderedKeys(){
        return Collections.list(keys());
    }
    public Enumeration<Object> keys() {
        return Collections.<Object>enumeration(keys);
    }

    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
   
    /*
    public static void readFile() throws IOException {
        File file = new File("C:\\Users\\DTBR6182\\Documents\\My Received Files\\reports1.properties");
        InputStream inputStream = new FileInputStream(file);
        LinkedProperties linkedProperties = new LinkedProperties();
        linkedProperties.load(inputStream);
        //Iterable<Object> iterator = linkedProperties.orderedKeys();
        //System.out.println(iterator);
        Enumeration names = linkedProperties.keys();
        while (names.hasMoreElements()){
            String name = (String)names.nextElement();
            System.out.println(name);
        }
        
    }

    public static void main(String[] args) throws IOException {
        readFile();
    } */

    

    
}

