/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.googlecode.objectify.annotation.Unindex;

import java.util.Date;

/**
 * Registration info.
 *
 * An account may be associated with multiple phones,
 * and a phone may be associated with multiple accounts.
 *
 * registrations lists different phones registered to that account.
 */
@Entity
public class Device {
    /**
     * User-email # device-id
     *
     * Device-id can be specified by device, default is hash of abs(registration
     * id).
     *
     * user@example.com#1234
     */
    @Id
    private String id;
    @Index
    private String accountName;
    /**
     * The ID used for sending messages to.
     */
    @Unindex
    private String deviceRegistrationId;
    /**
     * Current supported types:
     *   (default) - ac2dm, regular froyo+ devices using C2DM protocol
     *
     * New types may be defined - for example for sending to chrome.
     */
    @Unindex
    private String type;
    /**
     * For statistics - and to provide hints to the user.
     */
    @Unindex
    private Date registrationTimestamp;

    public Device() {
        registrationTimestamp = new Date();
    }

    public Device(String id, String accountName, String deviceRegistrationId, String type) {
        this();
        this.id = id;
        this.accountName = accountName;
        this.deviceRegistrationId = deviceRegistrationId;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getDeviceRegistrationId() {
        return deviceRegistrationId;
    }

    public void setDeviceRegistrationId(String deviceRegistrationId) {
        this.deviceRegistrationId = deviceRegistrationId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public void setRegistrationTimestamp(Date registrationTimestamp) {
        this.registrationTimestamp = registrationTimestamp;
    }

    public Date getRegistrationTimestamp() {
        return registrationTimestamp;
    }
}
