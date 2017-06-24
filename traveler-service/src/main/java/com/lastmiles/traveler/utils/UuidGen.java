/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lastmiles.traveler.utils;
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
