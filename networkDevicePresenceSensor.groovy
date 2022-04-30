/**
 *  Network device presence sensor v1.00
 *
 *  Copyright 2022 Andrzej Stolarczyk
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Release Notes:
 *  v1.00:  First version based on Hubitat-iPhone-Presence-Sensor by joelwetzel https://github.com/joelwetzel/Hubitat-iPhone-Presence-Sensor

 */

import groovy.json.*
    
metadata {
    definition (name: "Network device presence sensor", namespace: "andrzejstolarczyk", author: "Andrzej Stolarczyk") {
        capability "Refresh"
        capability "Sensor"
        capability "Presence Sensor"
    }

    preferences {
        section {
            input (
                type: "string",
                name: "ipAddress",
                title: "Device IP Address (with port)",
                description: "Define IP Address with port or without (default port is 80) e.g.: 192.168.1.10 or 192.168.1.10:9000",
                required: true                
            )
            input (
                type: "string",
                name: "protocol",
                title: "Protocol",
                required: true,
                defaultValue: "http"             
            )
              input (
                type: "string",
                name: "searchPhrase",
                title: "Search phrase",
                description: "You can define what phrase should your device return with the GET response",
                required: false                
            )
            input (
                type: "number",
                name: "timeoutMinutes",
                title: "Timeout Minutes",
                description: "Approximate number of minutes without a response before deciding the device is away/offline.",
                required: true,
                defaultValue: 3
            )
            input (
                type: "bool",
                name: "enableDebugLogging",
                title: "Enable Debug Logging?",
                required: true,
                defaultValue: true
            )
            input (
                type: "bool",
                name: "enableDevice",
                title: "Enable Device?",
                required: true,
                defaultValue: true
            )
        }
    }
}


def log(msg) {
    if (enableDebugLogging) {
        log.debug(msg)    
    }
}


def installed () {
    log.info "${device.displayName}.installed()"
    updated()
}


def updated () {
    log.info "${device.displayName}.updated()"
    
    state.tryCount = 0
    
    unschedule()
    
    if (enableDevice) {
        runEvery1Minute(refresh)        // Option 1: test it every minute.  Have a 10 second timeout on the requests.
        state.triesPerMinute = 1

    //schedule("*/15 * * * * ? *", refresh)    // Option 2: run every 15 seconds, but now we have a 10 second timeout on the requests.
        //state.triesPerMinute = 4
    }
    
    runIn(2, refresh)                // But test it once, right after we install or update it too.
}


def ensureStateVariables() {
    if (state.triesPerMinute == null) {
        state.triesPerMinute = 1
    }
}


def refresh() {
    log "${device.displayName}.refresh()";

    state.tryCount = state.tryCount + 1;
    
    ensureStateVariables();
    
    if ((state.tryCount / state.triesPerMinute) > (timeoutMinutes < 1 ? 1 : timeoutMinutes) && device.currentValue('presence') != "not present") {
        def descriptionText = "${device.displayName} is OFFLINE";
        log descriptionText;
        sendEvent(name: "presence", value: "not present", linkText: deviceName, descriptionText: descriptionText);
    }
    
    if (ipAddress == null || ipAddress.size() == 0) {
        return;
    }
    
    if (protocol == null || protocol.size() == 0) {
        return;
    }
    
    asynchttpGet("httpGetCallback", [
        uri: "${protocol}://${ipAddress}/",        
        ignoreSSLIssues: true, //if you're using https protocol and don't won't to bother about certificate error
        timeout: 10
    ]);
}


def httpGetCallback(response, data) {
        
    if (response != null && (response.status == 200  || (searchPhrase != null && searchPhrase.size() > 0 && response.data.toString().contains(searchPhrase)))) {
        state.tryCount = 0
        
        if (device.currentValue('presence') != "present") {
            def descriptionText = "${device.displayName} is ONLINE";
            log descriptionText
            sendEvent(name: "presence", value: "present", linkText: deviceName, descriptionText: descriptionText)
        }
    }
    else {
        log "${device.displayName}: OFFLINE, resposne status is ${response.status}"
    }
}