/*
 * openwms.org, the Open Warehouse Management System.
 * Copyright (C) 2014 Heiko Scherrer
 *
 * This file is part of openwms.org.
 *
 * openwms.org is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as 
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * openwms.org is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.openwms.tms;

import java.util.List;

import org.openwms.tms.target.Target;
import org.openwms.tms.target.TargetResolver;
import org.openwms.tms.voter.DecisionVoter;
import org.openwms.tms.voter.DeniedException;
import org.openwms.tms.voter.RedirectVote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A RedirectTO is responsible to handle target changes of a {@link TransportOrder}. Only the {@code targetLocationGroup} of the
 * TransportOrder {@code toUpdate} is recognized.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version 1.0
 * @since 1.0
 */
@Component
class RedirectTO implements UpdateFunction {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedirectTO.class);
    /** 0..* voters, can be overridden and extended with XML configuration. So far we define only one (default) voter directly. */
    @Autowired(required = false)
    private List<DecisionVoter<RedirectVote>> redirectVoters;
    @Autowired(required = false)
    private List<TargetResolver<Target>> targetResolvers;
    @Autowired
    private AddProblem addProblem;

    /**
     * {@inheritDoc}
     */
    @Override
    public void update(TransportOrder saved, TransportOrder toUpdate) {
        if (toUpdate.getTargetLocationGroup() != null && !toUpdate.getTargetLocationGroup().equals(saved.getTargetLocationGroup())) {

            // somehow the target changed...
            if (null != redirectVoters) {
                RedirectVote rv = new RedirectVote(toUpdate.getTargetLocationGroup(), saved);
                // TODO [openwms]: 13/07/16 the concept of a voter is misused in that a voter changes the state of a TO
                try {
                    for (DecisionVoter<RedirectVote> voter : redirectVoters) {
                        voter.voteFor(rv);
                    }
                } catch (DeniedException de) {
                    LOGGER.error("Could not redirect TransportOrder with persistent key [" + saved.getPersistentKey() + "], reason is: "
                            + de.getMessage());
                    addProblem.add(new Problem(de.getMessage()), saved);
                }
            }
//            targetResolvers.forEach(tr -> tr.resolve(toUpdate.getTargetLocationGroup()).);
            //            saved.setTargetLocationGroup(toUpdate.getTargetLocationGroup());
           // saved.setTargetLocation(toUpdate.getTargetLocation());
        }
    }
}
