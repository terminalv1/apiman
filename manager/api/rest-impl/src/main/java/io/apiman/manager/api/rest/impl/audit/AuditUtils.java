/*
 * Copyright 2014 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.apiman.manager.api.rest.impl.audit;

import io.apiman.manager.api.beans.apps.ApplicationBean;
import io.apiman.manager.api.beans.apps.ApplicationVersionBean;
import io.apiman.manager.api.beans.audit.AuditEntityType;
import io.apiman.manager.api.beans.audit.AuditEntryBean;
import io.apiman.manager.api.beans.audit.AuditEntryType;
import io.apiman.manager.api.beans.audit.data.ContractData;
import io.apiman.manager.api.beans.audit.data.EntityUpdatedData;
import io.apiman.manager.api.beans.audit.data.MembershipData;
import io.apiman.manager.api.beans.audit.data.PolicyData;
import io.apiman.manager.api.beans.contracts.ContractBean;
import io.apiman.manager.api.beans.orgs.OrganizationBean;
import io.apiman.manager.api.beans.plans.PlanBean;
import io.apiman.manager.api.beans.plans.PlanVersionBean;
import io.apiman.manager.api.beans.policies.PolicyBean;
import io.apiman.manager.api.beans.policies.PolicyType;
import io.apiman.manager.api.beans.services.ServiceBean;
import io.apiman.manager.api.beans.services.ServiceGatewayBean;
import io.apiman.manager.api.beans.services.ServicePlanBean;
import io.apiman.manager.api.beans.services.ServiceVersionBean;
import io.apiman.manager.api.security.ISecurityContext;

import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Contains a number of methods useful to create and manage audit entries for
 * actions taken by users in the management layer REST API.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(String before, String after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }

        if (before == null) {
            return true;
        }

        return !before.trim().equals(after.trim());
    }

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Boolean before, Boolean after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }

        if (before == null) {
            return true;
        }

        return !before.equals(after);
    }

    /**
     * Returns true only if the set has changed.
     *
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Set<?> before, Set<?> after) {
        if ((before == null && after == null) || after == null) {
            return false;
        }
        if (before == null) {
            if (after.isEmpty()) {
                return false;
            } else {
                return true;
            }
        } else {
            if (before.size() != after.size()) {
                return true;
            }
            for (Object bean : after) {
                if (!before.contains(bean)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns true only if the value changed.
     * @param before the value before change
     * @param after the value after change
     * @return true if value changed, else false
     */
    public static boolean valueChanged(Enum<?> before, Enum<?> after) {
        if (before == null && after == null) {
            return false;
        }
        if (after == null) {
            return false;
        }
        if (before == null && after != null) {
            return true;
        }
        return !(before == after);
    }

    /**
     * Creates an {@link AuditEntryBean} for the 'organization created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean organizationCreated(OrganizationBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'organization updated' event.
     * @param bean the bean
     * @param data the update
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean organizationUpdated(OrganizationBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getId(), AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'membership granted' even.
     * @param organizationId the organization id
     * @param data the membership data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean membershipGranted(String organizationId, MembershipData data,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(organizationId, AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Grant);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'membership revoked' even.
     * @param organizationId the organization id
     * @param data the membership data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean membershipRevoked(String organizationId, MembershipData data,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(organizationId, AuditEntityType.Organization, securityContext);
        entry.setEntityId(null);
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Revoke);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'service created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceCreated(ServiceBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceUpdated(ServiceBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'service version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceVersionCreated(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceVersionUpdated(ServiceVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry when a service definition is updated.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceDefinitionUpdated(ServiceVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.UpdateDefinition);
        return entry;
    }

    /**
     * Creates an audit entry when a service definition is deleted.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceDefinitionDeleted(ServiceVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.DeleteDefinition);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationCreated(ApplicationBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationUpdated(ApplicationBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'application version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationVersionCreated(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationVersionUpdated(ApplicationVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractCreatedFromApp(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getApplication().getApplication().getId());
        entry.setEntityVersion(bean.getApplication().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractCreatedToService(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        // Ensure the order of contract-created events are deterministic by adding 1 ms to this one
        entry.setCreatedOn(new Date(entry.getCreatedOn().getTime() + 1));
        entry.setWhat(AuditEntryType.CreateContract);
        entry.setEntityId(bean.getService().getService().getId());
        entry.setEntityVersion(bean.getService().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractBrokenFromApp(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getApplication().getApplication().getId());
        entry.setEntityVersion(bean.getApplication().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'contract broken' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean contractBrokenToService(ContractBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setWhat(AuditEntryType.BreakContract);
        entry.setEntityId(bean.getService().getService().getId());
        entry.setEntityVersion(bean.getService().getVersion());
        ContractData data = new ContractData(bean);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy added' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyAdded(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.AddPolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy removed' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyRemoved(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.RemovePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'policy updated' event.  Works for all
     * three kinds of policies.
     * @param bean the bean
     * @param type the policy type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policyUpdated(PolicyBean bean, PolicyType type,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganizationId(), null, securityContext);
        entry.setWhat(AuditEntryType.UpdatePolicy);
        entry.setEntityId(bean.getEntityId());
        entry.setEntityVersion(bean.getEntityVersion());
        switch (type) {
        case Application:
            entry.setEntityType(AuditEntityType.Application);
            break;
        case Plan:
            entry.setEntityType(AuditEntityType.Plan);
            break;
        case Service:
            entry.setEntityType(AuditEntityType.Service);
            break;
        }
        PolicyData data = new PolicyData();
        data.setPolicyDefId(bean.getDefinition().getId());
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Writes the data object as a JSON string.
     * @param data
     */
    private static String toJSON(Object data) {
        try {
            return mapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates an audit entry for the 'plan created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planCreated(PlanBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setData(null);
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planUpdated(PlanBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getId());
        entry.setEntityVersion(null);
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version created' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planVersionCreated(PlanVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Create);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan version updated' event.
     * @param bean the bean
     * @param data the updated data
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planVersionUpdated(PlanVersionBean bean, EntityUpdatedData data,
            ISecurityContext securityContext) {
        if (data.getChanges().isEmpty()) {
            return null;
        }
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Update);
        entry.setData(toJSON(data));
        return entry;
    }

    /**
     * Creates an audit entry for the 'service published' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean servicePublished(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Publish);
        return entry;
    }

    /**
     * Creates an audit entry for the 'service retired' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean serviceRetired(ServiceVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(bean.getService().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Retire);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application registered' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationRegistered(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Register);
        return entry;
    }

    /**
     * Creates an audit entry for the 'application unregistered' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean applicationUnregistered(ApplicationVersionBean bean,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(bean.getApplication().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Unregister);
        return entry;
    }

    /**
     * Creates an audit entry for the 'plan locked' event.
     * @param bean the bean
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean planLocked(PlanVersionBean bean, ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(bean.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(bean.getPlan().getId());
        entry.setEntityVersion(bean.getVersion());
        entry.setWhat(AuditEntryType.Lock);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a service.
     * @param svb the service and version
     * @param service the service type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(ServiceVersionBean svb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(svb.getService().getOrganization().getId(), AuditEntityType.Service, securityContext);
        entry.setEntityId(svb.getService().getId());
        entry.setEntityVersion(svb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in an application.
     * @param avb the application and version
     * @param service the service type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(ApplicationVersionBean avb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(avb.getApplication().getOrganization().getId(), AuditEntityType.Application, securityContext);
        entry.setEntityId(avb.getApplication().getId());
        entry.setEntityVersion(avb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Called when the user reorders the policies in a plan.
     * @param pvb the plan and version
     * @param service the service type
     * @param securityContext the security context
     * @return the audit entry
     */
    public static AuditEntryBean policiesReordered(PlanVersionBean pvb, PolicyType service,
            ISecurityContext securityContext) {
        AuditEntryBean entry = newEntry(pvb.getPlan().getOrganization().getId(), AuditEntityType.Plan, securityContext);
        entry.setEntityId(pvb.getPlan().getId());
        entry.setEntityVersion(pvb.getVersion());
        entry.setWhat(AuditEntryType.ReorderPolicies);
        return entry;
    }

    /**
     * Creates an audit entry.
     * @param orgId the organization id
     * @param type
     * @param securityContext the security context
     * @return the audit entry
     */
    private static AuditEntryBean newEntry(String orgId, AuditEntityType type, ISecurityContext securityContext) {
        // Wait for 1 ms to guarantee that two audit entries are never created at the same moment in time (which would
        // result in non-deterministic sorting by the storage layer)
        try { Thread.sleep(1); } catch (InterruptedException e) { throw new RuntimeException(e); }

        AuditEntryBean entry = new AuditEntryBean();
        entry.setOrganizationId(orgId);
        entry.setEntityType(type);
        entry.setCreatedOn(new Date());
        entry.setWho(securityContext.getCurrentUser());
        return entry;
    }

    /**
     * Converts the list of plans to a string for display/comparison.
     * @param plans the plans
     * @return the service plans as a string
     */
    public static String asString_ServicePlanBeans(Set<ServicePlanBean> plans) {
        TreeSet<ServicePlanBean> sortedPlans = new TreeSet<>(new Comparator<ServicePlanBean>() {
            @Override
            public int compare(ServicePlanBean o1, ServicePlanBean o2) {
                String p1 = o1.getPlanId() + ":" + o1.getVersion(); //$NON-NLS-1$
                String p2 = o2.getPlanId() + ":" + o2.getVersion(); //$NON-NLS-1$
                return p1.compareTo(p2);
            }
        });
        sortedPlans.addAll(plans);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (plans != null) {
            for (ServicePlanBean plan : sortedPlans) {
                if (!first) {
                    builder.append(", "); //$NON-NLS-1$
                }
                builder.append(plan.getPlanId()).append(":").append(plan.getVersion()); //$NON-NLS-1$
                first = false;
            }
        }
        return builder.toString();
    }


    /**
     * Converts the list of gateways to a string for display/comparison.
     * @param gateways set of gateways
     * @return the gateways as a string
     */
    public static String asString_ServiceGatewayBeans(Set<ServiceGatewayBean> gateways) {
        TreeSet<ServiceGatewayBean> sortedGateways = new TreeSet<>(new Comparator<ServiceGatewayBean>() {
            @Override
            public int compare(ServiceGatewayBean o1, ServiceGatewayBean o2) {
                return o1.getGatewayId().compareTo(o2.getGatewayId());
            }
        });
        sortedGateways.addAll(gateways);

        StringBuilder builder = new StringBuilder();
        boolean first = true;
        if (gateways != null) {
            for (ServiceGatewayBean gateway : sortedGateways) {
                if (!first) {
                    builder.append(", "); //$NON-NLS-1$
                }
                builder.append(gateway.getGatewayId());
                first = false;
            }
        }
        return builder.toString();
    }

}
