package com.lastmile.transport.utils;

import java.util.UUID;

/**
 *
 * @author David
 */
public class UuidGen {
    public static String generateUUID() {
        return UUID.randomUUID().toString();    
    }
}
