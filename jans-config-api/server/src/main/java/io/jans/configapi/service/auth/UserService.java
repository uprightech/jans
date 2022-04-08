/*
 * Janssen Project software is available under the Apache License (2004). See http://www.apache.org/licenses/ for full text.
 *
 * Copyright (c) 2020, Janssen Project
 */

package io.jans.configapi.service.auth;

import com.github.fge.jsonpatch.JsonPatchException;
import io.jans.as.common.model.common.User;
import io.jans.as.common.util.AttributeConstants;
import io.jans.as.model.config.StaticConfiguration;
import io.jans.as.model.configuration.AppConfiguration;
import io.jans.configapi.core.util.Jackson;
import io.jans.configapi.model.user.UserPatchRequest;
import io.jans.configapi.rest.model.SearchRequest;
import io.jans.orm.model.PagedResult;
import io.jans.orm.model.SortOrder;
import io.jans.orm.model.base.CustomObjectAttribute;
import io.jans.orm.search.filter.Filter;
import io.jans.util.StringHelper;

import static io.jans.as.model.util.Util.escapeLog;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;

@ApplicationScoped
@Named("userSrv")
public class UserService extends io.jans.as.common.service.common.UserService {

    @Inject
    private Logger logger;

    @Inject
    private StaticConfiguration staticConfiguration;

    @Inject
    private AppConfiguration appConfiguration;

    @Override
    public List<String> getPersonCustomObjectClassList() {
        return appConfiguration.getPersonCustomObjectClassList();
    }

    @Override
    public String getPeopleBaseDn() {
        return staticConfiguration.getBaseDn().getPeople();
    }

    public PagedResult<User> searchUsers(SearchRequest searchRequest) {
        if (logger.isDebugEnabled()) {
            logger.debug("Search Users with searchRequest:{}", escapeLog(searchRequest));
        }
        Filter searchFilter = null;
        if (StringUtils.isNotEmpty(searchRequest.getFilter())) {
            String[] targetArray = new String[] { searchRequest.getFilter() };
            Filter displayNameFilter = Filter.createSubstringFilter(AttributeConstants.DISPLAY_NAME, null, targetArray,
                    null);
            Filter descriptionFilter = Filter.createSubstringFilter(AttributeConstants.DESCRIPTION, null, targetArray,
                    null);
            Filter inumFilter = Filter.createSubstringFilter(AttributeConstants.INUM, null, targetArray, null);
            searchFilter = Filter.createORFilter(displayNameFilter, descriptionFilter, inumFilter);
        }

        return persistenceEntryManager.findPagedEntries(getPeopleBaseDn(), User.class, searchFilter, null,
                searchRequest.getSortBy(), SortOrder.getByValue(searchRequest.getSortOrder()),
                searchRequest.getStartIndex() - 1, searchRequest.getCount(), searchRequest.getMaxCount());

    }

    public void removeUser(User user) {
        persistenceEntryManager.removeRecursively(user.getDn(), User.class);
    }

    public User patchUser(String inum, UserPatchRequest userPatchRequest)
            throws JsonPatchException, IOException {

        logger.error("Details to patch user  inum:{}, UserPatchRequest:{} ", escapeLog(inum), escapeLog(userPatchRequest));
        if (StringHelper.isEmpty(inum)) {
            return null;
        }

        User user = getUserByInum(inum);
        if (user == null) {
            return null;
        }

        logger.error("User to be patched- user:{}", user);
        // apply direct patch for basic attributes
        if( StringUtils.isNotEmpty(userPatchRequest.getJsonPatchString())) {
            logger.error("Patch basic attributes");
            user = Jackson.applyPatch(userPatchRequest.getJsonPatchString(), user);
            logger.error("User after patching basic attributes - user:{}", user);
        }

        // patch for customAttributes
        if (userPatchRequest.getCustomAttributes() != null && !userPatchRequest.getCustomAttributes().isEmpty()) {
            user = updateCustomAttributes(user, userPatchRequest.getCustomAttributes());
        }

        logger.error("User before patch user:{}", user);

        // persist user
        user = updateUser(user);
        logger.error("User after patch user:{}", user);
        return user;

    }

    private User updateCustomAttributes(User user, List<CustomObjectAttribute> customAttributes) {
        logger.error("Custom Attributes to update for - user:{}, customAttributes:{} ", user, customAttributes);

        if (customAttributes != null && !customAttributes.isEmpty()) {
            for (CustomObjectAttribute attribute : customAttributes) {
                CustomObjectAttribute existingAttribute = getCustomAttribute(user, attribute.getName());
                logger.error("Existing CustomAttributes with existingAttribute:{} ", existingAttribute);

                // add
                if (existingAttribute == null) {
                    boolean result = addUserAttribute(user, attribute.getName(), attribute.getValues(),
                            attribute.isMultiValued());
                    logger.error("Result of adding CustomAttributes attribute:{} , result:{} ", attribute, result);
                }
                // remove attribute
                else if (attribute.getValue() == null || attribute.getValues() == null) {

                    user.removeAttribute(attribute.getName());
                }
                // replace attribute
                else {
                    existingAttribute.setMultiValued(attribute.isMultiValued());
                    existingAttribute.setValues(attribute.getValues());
                }
                // Final attribute
                logger.error("Finally user CustomAttributes user.getCustomAttributes:{} ", user.getCustomAttributes());

            }
        }

        return user;
    }

}
