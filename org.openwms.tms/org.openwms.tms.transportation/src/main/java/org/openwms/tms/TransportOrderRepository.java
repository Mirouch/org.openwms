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
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * A TransportOrderRepository provides functionality regarding {@link TransportOrder} entity classes.
 *
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @version 1.0
 * @see TransportOrder
 * @since 0.1
 */
public interface TransportOrderRepository extends JpaRepository<TransportOrder, Long> {

    @Query("select to from TransportOrder to where to.pKey = ?1")
    Optional<TransportOrder> findByPKey(String pKey);

    List<TransportOrder> findByBk(List<String> bks);

    @Query("select to from TransportOrder to where to.transportUnitBK = ?1 and to.state in ?2")
    List<TransportOrder> findByTransportUnitBKAndStates(String transportUnitBK, TransportOrder.State... states);

    List<TransportOrder> findByTargetLocation(String targetLocation);

    List<TransportOrder> findByTargetLocationGroup(String targetLocationGroup);
}
