/*
 * ElasticBox Confidential
 * Copyright (c) 2014 All Right Reserved, ElasticBox Inc.
 *
 * NOTICE:  All information contained herein is, and remains the property
 * of ElasticBox. The intellectual and technical concepts contained herein are
 * proprietary and may be covered by U.S. and Foreign Patents, patents in process,
 * and are protected by trade secret or copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from ElasticBox.
 */

package com.elasticbox.jenkins.util;

import com.elasticbox.Client;
import com.elasticbox.ClientException;
import com.elasticbox.jenkins.ElasticBoxCloud;
import hudson.slaves.Cloud;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import jenkins.model.Jenkins;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;

/**
 *
 * @author Phong Nguyen Le
 */
public class ClientCache {
    private static final Logger LOGGER = Logger.getLogger(ClientCache.class.getName());
    private static final ConcurrentHashMap<String, Client> clientCache = new ConcurrentHashMap<String, Client>();
    
    public static final Client findOrCreateClient(String cloudName) throws ClientException, IOException {
        Client client = clientCache.get(cloudName);
        if (client != null) {
            return client;
        }
        
        synchronized (clientCache) {
            // remove clients of deleted clouds
            for (Iterator<String> iter = clientCache.keySet().iterator(); iter.hasNext();) {
                String name = iter.next();
                if (Jenkins.getInstance().getCloud(name) == null) {
                    iter.remove();
                }
            }
            
            Cloud cloud = Jenkins.getInstance().getCloud(cloudName);        
            if (cloud instanceof ElasticBoxCloud) {
                client = new CachedClient((ElasticBoxCloud) cloud);
                client.connect();
                clientCache.put(cloudName, client);
            } else if (StringUtils.isNotBlank(cloudName)) {
                throw new IOException(MessageFormat.format("Invalid cloud name ''{0}''", cloudName));
            }
        }
        
        return client;        
    }
    
    public static final Client getClient(String cloudName) {
        try {
            return findOrCreateClient(cloudName);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, MessageFormat.format("Error creating client for ElasticBox cloud {0}", cloudName), ex);
        }
        
        return null;
    }

    public static Client getClient(String endpointUrl, String username, String password) {
        for (Cloud cloud : Jenkins.getInstance().clouds) {
            if (cloud instanceof ElasticBoxCloud) {
                ElasticBoxCloud ebCloud = (ElasticBoxCloud) cloud;
                if (ebCloud.getEndpointUrl().equals(endpointUrl) && ebCloud.getUsername().equals(username) &&
                        ebCloud.getPassword().equals(password)) {
                    return getClient(ebCloud.name);
                }
            }
        }
        
        return null;
    }

    public static void removeClient(ElasticBoxCloud cloud) {
        clientCache.remove(cloud.name);
    }
        
    private static final class CachedClient extends Client {
        private final String cloudName;

        public CachedClient(ElasticBoxCloud cloud) throws IOException {
            super(cloud.getEndpointUrl(), cloud.getUsername(), cloud.getPassword());
            cloudName = cloud.name;
        }

        @Override
        public void connect() throws IOException {
            try {
                super.connect();
            } catch (ClientException ex) {
                if (ex.getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                    clientCache.remove(cloudName);
                }
                
                throw ex;
            }
        }
        
        

        @Override
        protected HttpResponse execute(HttpRequestBase request) throws IOException {
            HttpResponse response = super.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {
                clientCache.remove(cloudName);
            }
            return response;
        }
    }
    
}