package com.tpandroid; 

import java.io.UnsupportedEncodingException;

import java.net.URLDecoder;
import java.net.URLEncoder;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;


public class SignedRequestsHelper {
    
    private static final String UTF8_CHARSET = "UTF-8";
    
   
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    
    private static final String REQUEST_URI = "/onca/xml";
    
   
    private static final String REQUEST_METHOD = "GET";

    private String endpoint = null;
    private String awsAccessKeyId = null;
    private String awsSecretKey = null;

    private SecretKeySpec secretKeySpec = null;
    private Mac mac = null;

    
    public static SignedRequestsHelper getInstance(
            String endpoint, 
            String awsAccessKeyId, 
            String awsSecretKey
    ) throws IllegalArgumentException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException
    {
        if (null == endpoint || endpoint.length() == 0)
            { throw new IllegalArgumentException("endpoint is null or empty"); }
        if (null == awsAccessKeyId || awsAccessKeyId.length() == 0) 
            { throw new IllegalArgumentException("awsAccessKeyId is null or empty"); }
        if (null == awsSecretKey || awsSecretKey.length() == 0)   
            { throw new IllegalArgumentException("awsSecretKey is null or empty"); }
        
        SignedRequestsHelper instance = new SignedRequestsHelper();
        instance.endpoint = endpoint.toLowerCase();
        instance.awsAccessKeyId = awsAccessKeyId;
        instance.awsSecretKey = awsSecretKey;

        byte[] secretyKeyBytes = instance.awsSecretKey.getBytes(UTF8_CHARSET);
        instance.secretKeySpec = new SecretKeySpec(secretyKeyBytes, HMAC_SHA256_ALGORITHM);
        instance.mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
        instance.mac.init(instance.secretKeySpec);

        return instance;
    }
    
  
    private SignedRequestsHelper() {}

   
    public String sign(Map<String, String> params) {
        params.put("AWSAccessKeyId", this.awsAccessKeyId);
        params.put("Timestamp", this.timestamp());

        SortedMap<String, String> sortedParamMap = new TreeMap<String, String>(params);
        
        String canonicalQS = this.canonicalize(sortedParamMap);
        
        String toSign = 
            REQUEST_METHOD + "\n" 
            + this.endpoint + "\n"
            + REQUEST_URI + "\n"
            + canonicalQS;

        String hmac = this.hmac(toSign);
        String sig = this.percentEncodeRfc3986(hmac);

        String url = 
            "http://" + this.endpoint + REQUEST_URI + "?" + canonicalQS + "&Signature=" + sig;

        return url;
    }

   
    public String sign(String queryString) {
        // let's break the query string into it's constituent name-value pairs
        Map<String, String> params = this.createParameterMap(queryString);
        
        // then we can sign the request as before
        return this.sign(params);
    }

    private String hmac(String stringToSign) {
        String signature = null;
        byte[] data;
        byte[] rawHmac;
        try {
            data = stringToSign.getBytes(UTF8_CHARSET);
            rawHmac = mac.doFinal(data);
            Base64 encoder = new Base64();
            signature = new String(encoder.encode(rawHmac));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(UTF8_CHARSET + " is unsupported!", e);
        }
        return signature;
    }

    
    private String timestamp() {
        String timestamp = null;
        Calendar cal = Calendar.getInstance();
        DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        dfm.setTimeZone(TimeZone.getTimeZone("GMT"));
        timestamp = dfm.format(cal.getTime());
        return timestamp;
    }

    
    private String canonicalize(SortedMap<String, String> sortedParamMap) {
        if (sortedParamMap.isEmpty()) {
            return "";
        }

        StringBuffer buffer = new StringBuffer();
        Iterator<Map.Entry<String, String>> iter = sortedParamMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry<String, String> kvpair = iter.next();
            buffer.append(percentEncodeRfc3986(kvpair.getKey()));
            buffer.append("=");
            buffer.append(percentEncodeRfc3986(kvpair.getValue()));
            if (iter.hasNext()) {
                buffer.append("&");
            }
        }
        String cannoical = buffer.toString();
        return cannoical;
    }

  
    private String percentEncodeRfc3986(String s) {
        String out;
        try {
            out = URLEncoder.encode(s, UTF8_CHARSET)
                .replace("+", "%20")
                .replace("*", "%2A")
                .replace("%7E", "~");
        } catch (UnsupportedEncodingException e) {
            out = s;
        }
        return out;
    }

   
    private Map<String, String> createParameterMap(String queryString) {
        Map<String, String> map = new HashMap<String, String>();
        String[] pairs = queryString.split("&");

        for (String pair: pairs) {
            if (pair.length() < 1) {
                continue;
            }

            String[] tokens = pair.split("=",2);
            for(int j=0; j<tokens.length; j++)
            {
                try {
                    tokens[j] = URLDecoder.decode(tokens[j], UTF8_CHARSET);
                } catch (UnsupportedEncodingException e) {
                }
            }
            switch (tokens.length) {
                case 1: {
                    if (pair.charAt(0) == '=') {
                        map.put("", tokens[0]);
                    } else {
                        map.put(tokens[0], "");
                    }
                    break;
                }
                case 2: {
                    map.put(tokens[0], tokens[1]);
                    break;
                }
            }
        }
        return map;
    }
}
