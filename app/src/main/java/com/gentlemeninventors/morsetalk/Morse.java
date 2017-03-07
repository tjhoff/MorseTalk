package com.gentlemeninventors.morsetalk;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tj on 3/5/17.
 */

public class Morse {

    static private Map<String, String> morse_to_text;
    static {
        morse_to_text = new HashMap<>();
        morse_to_text.put("A", ".-");
        morse_to_text.put("B", "-...");
        morse_to_text.put("C", "-.-.");
        morse_to_text.put("D", "-..");
        morse_to_text.put("E", ".");
        morse_to_text.put("F", "..-.");
        morse_to_text.put("G", "--.");
        morse_to_text.put("H", "....");
        morse_to_text.put("I", "..");
        morse_to_text.put("J", ".---");
        morse_to_text.put("K", "-.-");
        morse_to_text.put("L", ".-..");
        morse_to_text.put("M", "--");
        morse_to_text.put("N", "-.");
        morse_to_text.put("O", "---");
        morse_to_text.put("P", ".--.");
        morse_to_text.put("Q", "--.-");
        morse_to_text.put("R", ".-.");
        morse_to_text.put("S", "...");
        morse_to_text.put("T", "-");
        morse_to_text.put("U", "..-");
        morse_to_text.put("V", "...-");
        morse_to_text.put("W", ".--");
        morse_to_text.put("X", "-..-");
        morse_to_text.put("Y", "-.--");
        morse_to_text.put("Z", "--..");
        morse_to_text.put("0", "-----");
        morse_to_text.put("1", ".----");
        morse_to_text.put("2", "..---");
        morse_to_text.put("3", "...--");
        morse_to_text.put("4", "...._");
        morse_to_text.put("5", ".....");
        morse_to_text.put("6", "-....");
        morse_to_text.put("7", "--...");
        morse_to_text.put("8", "---..");
        morse_to_text.put("9", "----.");
        morse_to_text.put(".", ".-.-.-");
        morse_to_text.put(",", "--..--");
        morse_to_text.put(";", "---...");
        morse_to_text.put("?", "..--..");
        morse_to_text.put("@", ".--.-.");
        morse_to_text.put(" ", "|");
    }

    static public String convertText(String text){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < text.length(); i++){
            char c = text.charAt(i);
            String code = morse_to_text.get(String.valueOf(c).toUpperCase());
            String morseCode;
            if (code == null){
                morseCode = " ";
            }
            else{
                morseCode = code + " ";
            }
            sb.append(morseCode);
        }
        
        return sb.toString();
    }
}
