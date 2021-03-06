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
package org.openwms.common.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ameba.annotation.TxService;
import org.ameba.exception.NotFoundException;
import org.ameba.exception.ServiceLayerException;
import org.openwms.common.location.Location;
import org.openwms.common.location.LocationPK;
import org.openwms.common.location.LocationService;
import org.openwms.core.listener.OnRemovalListener;
import org.openwms.core.listener.RemovalNotAllowedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.transaction.annotation.Transactional;

/**
 * A TransportUnitServiceImpl.
 * 
 * @author <a href="mailto:scherrer@openwms.org">Heiko Scherrer</a>
 * @since 0.1
 */
@TxService
class TransportUnitServiceImpl implements TransportUnitService<TransportUnit> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportUnitServiceImpl.class);

    @Autowired
    private TransportUnitRepository dao;

    @Autowired
    private LocationService<Location> locationService;

    @Autowired
    private TransportUnitTypeRepository transportUnitTypeRepository;

    @Autowired(required = false)
    @Qualifier("onRemovalListener")
    private OnRemovalListener<TransportUnit> onRemovalListener;

    /**
     * Attach an OnRemovalListener.
     * 
     * @param onRemovalListener
     *            The listener to attach
     */
    void setOnRemovalListener(OnRemovalListener<TransportUnit> onRemovalListener) {
        this.onRemovalListener = onRemovalListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TransportUnit create(Barcode barcode, TransportUnitType transportUnitType, LocationPK actualLocation) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Creating a TransportUnit with Barcode " + barcode + " of Type " + transportUnitType.getType()
                    + " on Location " + actualLocation);
        }
        TransportUnit transportUnit = dao.findByBarcode(barcode).get();
        if (transportUnit != null) {
            throw new ServiceLayerException("TransportUnit with id " + barcode + " not found");
        }
        Location location = locationService.findByLocationId(actualLocation);
        if (location == null) {
            throw new ServiceLayerException("Location " + actualLocation + " not found");
        }
        TransportUnitType type = transportUnitTypeRepository.findByType(transportUnitType.getType()).get();
        if (null == type) {
            throw new ServiceLayerException("TransportUnitType " + transportUnitType + " not found");
        }
        transportUnit = new TransportUnit(barcode);
        transportUnit.setTransportUnitType(type);
        transportUnit.setActualLocation(location);
        dao.save(transportUnit);
        return transportUnit;
    }

    @Override
    public TransportUnit update(Barcode barcode, TransportUnit tu) {
        TransportUnit savedTu = dao.findByBarcode(barcode).orElseThrow(NotFoundException::new);
        //if (savedTu.get)
        // TODO [openwms]: 25/07/16

        return savedTu;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public List<TransportUnit> findAll() {
        return dao.findAll();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void moveTransportUnit(Barcode barcode, LocationPK targetLocationPK) {
        TransportUnit transportUnit = dao.findByBarcode(barcode).get();
        if (transportUnit == null) {
            throw new ServiceLayerException("TransportUnit with id " + barcode + " not found");
        }
        Location actualLocation = locationService.findByLocationId(targetLocationPK);
        // if (actualLocation == null) {
        // throw new ServiceException("Location with id " + newLocationPk +
        // " not found");
        // }
        transportUnit.setActualLocation(actualLocation);
        // try {
        dao.save(transportUnit);
        // }
        // catch (RuntimeException e) {
        // throw new ServiceException("Cannot move TransportUnit with barcode "
        // + barcode + " to location "
        // + newLocationPk, e);
        // }
    }

    /**
     * {@inheritDoc}
     * 
     * A ServiceRuntimeException is thrown when other {@link TransportUnit}s are placed on a {@link TransportUnit} that shall be removed.
     * Also {@link TransportUnit} with active TransportOrders won't be removed, if a proper delegate exists.
     */
    @Override
    public void deleteTransportUnits(List<TransportUnit> transportUnits) {
        if (transportUnits != null && transportUnits.size() > 0) {
            // FIXME [openwms]: 29/04/16 !!!!
//            List<TransportUnit> tus = ServiceHelper.managedEntities(transportUnits, dao);
            List<TransportUnit> tus = new ArrayList<>();
            // FIXME [openwms]: 29/04/16


            // first try to delete depending ones, afterwards the parent
            // units...
            Collections.sort(tus, new Comparator<TransportUnit>() {
                @Override
                public int compare(TransportUnit o1, TransportUnit o2) {
                    return o1.getChildren().isEmpty() ? -1 : 1;
                };
            });
            for (TransportUnit tu : tus) {
                if (!tu.getChildren().isEmpty()) {
                    throw new ServiceLayerException("Other TransportUnits are placed on this TransportUnit");
                }
                try {
                    delete(tu);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Successfully marked TransportUnit for removal : " + tu.getPk());
                    }
                } catch (RemovalNotAllowedException rnae) {
                    LOGGER.error("Not allowed to remove TransportUnit with id : " + tu.getPk() + " with reason: "
                            + rnae.getLocalizedMessage());
                    throw new ServiceLayerException(rnae.getLocalizedMessage());
                }
            }
        }
    }

    /**
     * Try to remove when there is no listener defined or a defined listener votes for removal.
     * 
     * @param transportUnit
     *            The TransportUnit to be removed
     * @throws RemovalNotAllowedException
     *             In case it is not allowed to remove the TransportUnit, probably because depending items exist (like TransportOrders).
     */
    private void delete(TransportUnit transportUnit) throws RemovalNotAllowedException {
        if (LOGGER.isDebugEnabled() && onRemovalListener == null) {
            LOGGER.debug("No listener onRemove defined, just try to delete it");
        }
        if (null == onRemovalListener || onRemovalListener.preRemove(transportUnit)) {
            dao.delete(transportUnit);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public TransportUnit findByBarcode(Barcode barcode) {
        return dao.findByBarcode(barcode).orElseThrow(NotFoundException::new);
    }
}