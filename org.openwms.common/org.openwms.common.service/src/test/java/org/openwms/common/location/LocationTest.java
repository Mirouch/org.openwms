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
package org.openwms.common.location;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * A LocationDaoTest.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version $Revision$
 * @since 0.1
 */
public class LocationTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    public
    @Test
    void testConstructionWithNull() {
        thrown.expect(IllegalArgumentException.class);
        new Location(null);
    }

    public
    @Test
    void testDefaultCreation() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        assertThat(l.getNoMaxTransportUnits()).isEqualTo(Location.DEF_MAX_TU);
        assertThat(l.isCountingActive()).isFalse();
        assertThat(l.getCheckState()).isEqualTo(Location.DEF_CHECK_STATE);
        assertThat(l.isLocationGroupCountingActive()).isEqualTo(Location.DEF_LG_COUNTING_ACTIVE);
        assertThat(l.isIncomingActive()).isEqualTo(Location.DEF_INCOMING_ACTIVE);
        assertThat(l.isOutgoingActive()).isEqualTo(Location.DEF_OUTGOING_ACTIVE);
        assertThat(l.getPlcState()).isEqualTo(Location.DEF_PLC_STATE);
        assertThat(l.isConsideredInAllocation()).isEqualTo(Location.DEF_CONSIDERED_IN_ALLOCATION);
    }

    public
    @Test
    void testAddMessageWithNull() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        thrown.expect(IllegalArgumentException.class);
        l.addMessage(null);
    }

    public
    @Test
    void testMessageHandling() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        Message m = Message.newBuilder().messageNo(1).messageText("First message").build();

        l.addMessage(m);
        assertThat(l.getMessages()).hasSize(1);
        assertThat(l.getMessages()).contains(m);

        l.removeMessages(m);
        assertThat(l.getMessages()).hasSize(0);
    }

    public
    @Test
    void testRemoveMessageWithNull() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        thrown.expect(IllegalArgumentException.class);
        l.removeMessages(null);
    }

    public
    @Test
    void testSetLocationGroupWithNull() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        thrown.expect(IllegalArgumentException.class);
        l.setLocationGroup(null);
    }

    public
    @Test
    void testSetLocationGroup() {
        Location l = new Location(new LocationPK.Builder().area("area").aisle("aisle").x("x").y("y").z("z").build());
        assertThat(l.getLocationGroup()).isNull();
        LocationGroup lg1 = new LocationGroup("error zone");
        LocationGroup lg2 = new LocationGroup("picking");
        lg2.setLocationGroupCountingActive(false);

        l.setLocationGroup(lg1);
        assertThat(l.getLocationGroup()).isEqualTo(lg1);
        assertThat(l.isLocationGroupCountingActive()).isEqualTo(lg1.isLocationGroupCountingActive());
        l.setLocationGroup(lg2);
        assertThat(l.getLocationGroup()).isEqualTo(lg2);
        assertThat(l.isLocationGroupCountingActive()).isEqualTo(lg2.isLocationGroupCountingActive());
    }

    /**
     * Test adding messages to the location. Test cascading persistence.
     @Test public final void testAddingErrorMessages() {
     LocationPK pk = new LocationPK("OWN", "OWN", "OWN", "OWN", "OWN");
     Location location = new Location(pk);
     locationRepository.save(location);

     location.addMessage(new Message(1, "Errormessage 1"));
     location.addMessage(new Message(2, "Errormessage 2"));

     locationRepository.save(location);

     Assert.assertEquals("Expected 2 persisted Messages added to the location", 2, location.getMessages().size());
     Query query = entityManager.createQuery("select count(m) from Message m");
     Long cnt = (Long) query.getSingleResult();
     Assert.assertEquals("Expected 2 persisted Messages in MESSAGE table", 2, cnt.intValue());
     }
     */
}
