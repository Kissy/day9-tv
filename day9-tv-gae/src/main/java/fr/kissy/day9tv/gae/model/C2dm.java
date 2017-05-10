/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package fr.kissy.day9tv.gae.model;

import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Unindex;

/**
 * Persistent config info for the server - authentication token
 */
@Entity
public final class C2dm {

    public static final String DATAMESSAGING_SEND_ENDPOINT = "https://android.clients.google.com/c2dm/send";

    @Id
    private Long id;
    @Unindex
    private String authToken;
    @Unindex
    private String c2dmUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getC2DMUrl() {
        if (c2dmUrl == null) {
            return DATAMESSAGING_SEND_ENDPOINT;
        } else {
            return c2dmUrl;
        }
    }

    public void setC2dmUrl(String c2dmUrl) {
        this.c2dmUrl = c2dmUrl;
    }
}