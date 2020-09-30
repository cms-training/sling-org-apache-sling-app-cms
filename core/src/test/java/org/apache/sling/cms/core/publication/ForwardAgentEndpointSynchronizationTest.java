/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sling.cms.core.publication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.sling.discovery.InstanceDescription;
import org.apache.sling.discovery.TopologyEvent;
import org.apache.sling.discovery.TopologyView;
import org.junit.Test;
import org.mockito.Mockito;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class ForwardAgentEndpointSynchronizationTest {

    @Test
    public void testHandleTopologyEvent() throws IOException, InvalidSyntaxException {
        TopologyEvent event = Mockito.mock(TopologyEvent.class);

        TopologyView view = Mockito.mock(TopologyView.class);

        InstanceDescription description = Mockito.mock(InstanceDescription.class);
        Mockito.when(description.getProperty(Mockito.matches(InstanceDescription.PROPERTY_ENDPOINTS)))
                .thenReturn("https://sling.apache.org");
        Mockito.when(description.getProperty(Mockito.matches(PublicationPropertyProvider.ENDPOINT_PATHS)))
                .thenReturn("/libs/distribute");
        Mockito.when(view.findInstances(Mockito.any())).thenReturn(Collections.singleton(description));
        Mockito.when(event.getNewView()).thenReturn(view);

        ConfigurationAdmin configAdmin = Mockito.mock(ConfigurationAdmin.class);
        Configuration sampleConfig = new Configuration() {
            private Dictionary<String, Object> properties = new Hashtable<>();

            @Override
            public String getPid() {
                return "org.apache.sling";
            }

            @Override
            public Dictionary<String, Object> getProperties() {
                return properties;
            }

            @Override
            public void update(Dictionary<String, ?> properties) throws IOException {
                this.properties = (Dictionary<String, Object>) properties;
            }

            @Override
            public void delete() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getFactoryPid() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void update() throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setBundleLocation(String location) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String getBundleLocation() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long getChangeCount() {
                throw new UnsupportedOperationException();
            }
        };
        Mockito.when(configAdmin.listConfigurations(Mockito.any())).thenReturn(new Configuration[] { sampleConfig });

        ForwardAgentEndpointSynchronization sync = new ForwardAgentEndpointSynchronization(configAdmin,
                new ForwardAgentEndpointSynchronizationConfig() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }

                    @Override
                    public String agentTarget() {
                        return null;
                    }

                });
        sync.handleTopologyEvent(event);
        assertTrue(Arrays.equals(new String[] { "https://sling.apache.org/libs/distribute" },
                (String[]) sampleConfig.getProperties().get(ForwardAgentEndpointSynchronization.ENDPOINT_PROPERTY)));
    }

    @Test
    public void testNoConfigurations() throws IOException, InvalidSyntaxException {
        TopologyEvent event = Mockito.mock(TopologyEvent.class);

        TopologyView view = Mockito.mock(TopologyView.class);
        Mockito.when(event.getNewView()).thenReturn(view);

        ConfigurationAdmin configAdmin = Mockito.mock(ConfigurationAdmin.class);
        Mockito.when(configAdmin.listConfigurations(Mockito.any())).thenReturn(null);

        ForwardAgentEndpointSynchronization sync = new ForwardAgentEndpointSynchronization(configAdmin,
                new ForwardAgentEndpointSynchronizationConfig() {

                    @Override
                    public Class<? extends Annotation> annotationType() {
                        return null;
                    }

                    @Override
                    public String agentTarget() {
                        return null;
                    }

                });
        sync.handleTopologyEvent(event);
        assertFalse(false);
    }

}