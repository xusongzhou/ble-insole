/******************************************************************************
 *  Copyright (C) Cambridge Silicon Radio Limited 2014
 *
 *  This software is provided to the customer for evaluation
 *  purposes only and, as such early feedback on performance and operation
 *  is anticipated. The software source code is subject to change and
 *  not intended for production. Use of developmental release software is
 *  at the user's own risk. This software is provided "as is," and CSR
 *  cautions users to determine for themselves the suitability of using the
 *  beta release version of this software. CSR makes no warranty or
 *  representation whatsoever of merchantability or fitness of the product
 *  for any particular purpose or use. In no event shall CSR be liable for
 *  any consequential, incidental or special damages whatsoever arising out
 *  of the use of or inability to use this software, even if the user has
 *  advised CSR of the possibility of such damages.
 *
 ******************************************************************************/

package com.advanpro.fwtools.ble;

import android.os.Handler;

import java.util.HashMap;
import java.util.UUID;

public class CharacteristicHandlersContainer {
    private HashMap<String, HashMap<UUID, HashMap<UUID, Handler>>> mHandlers = new HashMap<>();
    
    public void addHandler(String deviceMac, UUID service, UUID characteristic, Handler notifyHandler) {
        HashMap<UUID, HashMap<UUID, Handler>> subMap = mHandlers.get(deviceMac);
        if (subMap == null) {
            subMap = new HashMap<>();
            mHandlers.put(deviceMac, subMap);
        }
        
        HashMap<UUID, Handler> secondSubMap = subMap.get(service);
        if (secondSubMap == null) {
            secondSubMap = new HashMap<>();
            subMap.put(service, secondSubMap);
        }
        secondSubMap.put(characteristic, notifyHandler);
    }

    public void removeHandler(String deviceMac, UUID service, UUID characteristic) {
        HashMap<UUID, HashMap<UUID, Handler>> subMap = mHandlers.get(deviceMac);
        if (subMap != null) {            
            HashMap<UUID, Handler> secondSubMap = subMap.get(service);
            if (secondSubMap != null) {
                secondSubMap.remove(characteristic);
            }
        }        
    }

    public Handler getHandler(String deviceMac, UUID service, UUID characteristic) {
        HashMap<UUID, HashMap<UUID, Handler>> subMap = mHandlers.get(deviceMac);
        if (subMap == null) return null;   
        HashMap<UUID, Handler> secondSubMap = subMap.get(service);
        if (secondSubMap == null) return null;
        return secondSubMap.get(characteristic);
    }

}