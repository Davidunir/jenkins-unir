/*
 *
 *  ElasticBox Confidential
 *  Copyright (c) 2016 All Right Reserved, ElasticBox Inc.
 *
 *  NOTICE:  All information contained herein is, and remains the property
 *  of ElasticBox. The intellectual and technical concepts contained herein are
 *  proprietary and may be covered by U.S. and Foreign Patents, patents in process,
 *  and are protected by trade secret or copyright law. Dissemination of this
 *  information or reproduction of this material is strictly forbidden unless prior
 *  written permission is obtained from ElasticBox.
 *
 */

package com.elasticbox.jenkins.model.services.instances.execution.context;

import com.elasticbox.jenkins.model.services.instances.execution.order.UpdateInstancesOrder;


public class UpdateInstancesContext extends AbstractManageInstancesContext<UpdateInstancesOrder> {

    private UpdateInstancesContext(UpdateInstancesContextBuilder builder) {
        super(builder);
    }


    public static class UpdateInstancesContextBuilder
        extends ManageInstancesContextBuilder<UpdateInstancesContextBuilder, UpdateInstancesContext> {

        @Override
        public UpdateInstancesContext build() {
            return new UpdateInstancesContext(this);
        }
    }
}
