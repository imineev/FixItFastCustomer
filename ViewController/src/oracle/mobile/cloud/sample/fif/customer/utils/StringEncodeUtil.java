package oracle.mobile.cloud.sample.fif.customer.utils;

import java.nio.charset.StandardCharsets;

import java.util.Base64;

/**
 *  *** INTERNAL USE ONLY  ***
 *
 * @author   Frank Nimphius
 * @copyright Copyright (c) 2015 Oracle. All rights reserved.
 */
public class StringEncodeUtil {
    public StringEncodeUtil() {
        super();
    }


    /**
     * Provides a base64 encoding for strings
     * @param stringToEncode
     * @return
     */
    public static String base64Encode(String stringToEncode) {
        byte[] _bytes = stringToEncode.getBytes(StandardCharsets.UTF_8);
        String base64EncodedString = Base64.getEncoder().encodeToString(_bytes);
        
        return base64EncodedString;
    }
    
    
    /**
     * Provides a base64 decoding for strings
     * @param stringToDecode
     * @return
     */
    public static String base64Decode(String stringToDecode) {
        byte[] _bytes = Base64.getDecoder().decode(stringToDecode);
        String base64DecodedString = "";
        base64DecodedString = new String(_bytes, StandardCharsets.UTF_8);
        return base64DecodedString;
    }
    
    
}

