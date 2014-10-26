/*
 * Created by Brian Green on 2014.10.25  * 
 * Copyright © 2014 Brian Green. All rights reserved. * 
 */

package com.brigreen.sessionbeanpackage;

import com.brigreen.planneroftheapes.Assignment;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author Brian
 */
@Stateless
public class AssignmentFacade extends AbstractFacade<Assignment> {
    @PersistenceContext(unitName = "planneroftheapesPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public AssignmentFacade() {
        super(Assignment.class);
    }
    
}
