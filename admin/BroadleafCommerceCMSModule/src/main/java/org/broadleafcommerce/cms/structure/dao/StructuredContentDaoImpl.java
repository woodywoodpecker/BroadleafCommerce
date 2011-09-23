/*
 * Copyright 2008-20011 the original author or authors.
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
package org.broadleafcommerce.cms.structure.dao;

import org.broadleafcommerce.cms.locale.domain.Locale;
import org.broadleafcommerce.cms.structure.domain.StructuredContent;
import org.broadleafcommerce.cms.structure.domain.StructuredContentField;
import org.broadleafcommerce.cms.structure.domain.StructuredContentType;
import org.broadleafcommerce.openadmin.server.domain.SandBox;
import org.broadleafcommerce.openadmin.server.domain.SandBoxImpl;
import org.broadleafcommerce.openadmin.time.SystemTime;
import org.broadleafcommerce.persistence.EntityConfiguration;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bpolster.
 */
@Repository("blStructuredContentDao")
public class StructuredContentDaoImpl implements StructuredContentDao {

    private static SandBox DUMMY_SANDBOX = new SandBoxImpl();
    {
        DUMMY_SANDBOX.setId(-1l);
    }

    @PersistenceContext(unitName = "blPU")
    protected EntityManager em;

    @Resource(name="blEntityConfiguration")
    protected EntityConfiguration entityConfiguration;

    @Override
    public StructuredContent findStructuredContentById(Long contentId) {
        return (StructuredContent) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.cms.structure.domain.StructuredContent"), contentId);
    }

    @Override
    public StructuredContentType findStructuredContentTypeById(Long contentTypeId) {
        return (StructuredContentType) em.find(entityConfiguration.lookupEntityClass("org.broadleafcommerce.cms.structure.domain.StructuredContentType"), contentTypeId);
    }

    @Override
    public List<StructuredContentType> retrieveAllStructuredContentTypes() {
        Query query = em.createNamedQuery("BC_READ_ALL_STRUCTURED_CONTENT_TYPES");
        return query.getResultList();
    }

    @Override
    public Map<String, StructuredContentField> readFieldsForStructuredContentItem(StructuredContent sc) {
        Query query = em.createNamedQuery("BC_READ_CONTENT_FIELDS_BY_CONTENT_ID");
        query.setParameter("structuredContent", sc);

        List<StructuredContentField> fields = (List<StructuredContentField>) query.getResultList();
        Map<String, StructuredContentField> fieldMap = new HashMap<String, StructuredContentField>();
        for (StructuredContentField scField : fields) {
            fieldMap.put(scField.getFieldKey(), scField);
        }
        return fieldMap;
    }

    @Override
    public StructuredContent addOrUpdateContentItem(StructuredContent content) {
        em.clear();
        return em.merge(content);
    }

    @Override
    public void delete(StructuredContent content) {
        if (! em.contains(content)) {
            content = findStructuredContentById(content.getId());
        }
        em.remove(content);
    }

    @Override
    public List<StructuredContent> findActiveStructuredContentByType(SandBox sandBox, StructuredContentType type, Locale locale) {
        String queryName = "BC_ACTIVE_STRUCTURED_CONTENT_BY_TYPE";
        if (sandBox != null) {
            queryName =  "BC_ACTIVE_STRUCTURED_CONTENT_BY_TYPE_AND_SANDBOX";
        }

        Query query = em.createNamedQuery(queryName);
        query.setParameter("contentType", type);
        query.setParameter("currentDate", SystemTime.asDate());
        query.setParameter("locale", locale);
        if (sandBox != null) {
            query.setParameter("sandbox", sandBox);
        }
        return query.getResultList();
    }

    @Override
    public List<StructuredContent> findActiveStructuredContentByNameAndType(SandBox sandBox, StructuredContentType type, String name, Locale locale) {
        String queryName = "BC_ACTIVE_STRUCTURED_CONTENT_BY_TYPE_AND_NAME";
        if (sandBox != null) {
            queryName =  "BC_ACTIVE_STRUCTURED_CONTENT_BY_TYPE_AND_NAME_AND_SANDBOX";
        }

        Query query = em.createNamedQuery(queryName);
        query.setParameter("contentType", type);
        query.setParameter("contentName", name);
        query.setParameter("currentDate", SystemTime.asDate());
        query.setParameter("locale", locale);
        if (sandBox != null) {
            query.setParameter("sandbox", sandBox);
        }
        return query.getResultList();
    }

    @Override
    public StructuredContentType findStructuredContentTypeByName(String name) {
        Query query = em.createNamedQuery("BC_READ_STRUCTURED_CONTENT_TYPE_BY_NAME");
        query.setParameter("name",name);
        List<StructuredContentType> results = query.getResultList();
        if (results.size() > 0) {
            return results.get(0);
        } else {
            return null;
        }

    }
}
