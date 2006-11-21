package org.osaf.cosmo.service.impl;

import org.osaf.cosmo.dao.UserDao;
import org.osaf.cosmo.model.User;
import org.osaf.cosmo.service.AccountActivationService;

public abstract class AbstractCosmoAccountActivationService implements
        AccountActivationService {

    private UserDao userDao;

    public void setUserDao(UserDao userDao){
        this.userDao = userDao;
    }

    /**
     * Given an activation token, look up and activate a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public void activateUserFromToken(String activationToken) {
        getUserFromToken(activationToken).activate();
    }

    /**
     * Given an activation token, look up and return a user.
     *
     * @param activationToken
     * @throws DataRetrievalFailureException if the token does
     * not correspond to any users
     */
    public User getUserFromToken(String activationToken){
        return this.userDao.getUserByActivationId(activationToken);
    }
}
