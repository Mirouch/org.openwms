/*
 * OpenWMS, the open Warehouse Management System
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.openwms.common.dao;

/**
 * A DaoException.
 * 
 * @author <a href="heiko.scherrer@gmx.de">Heiko Scherrer</a>
 * @version $Revision: 314 $
 */
public class DaoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new DaoException.
	 * 
	 */
	public DaoException() {
		super();
	}

	/**
	 * Create a new DaoException.
	 * 
	 * @param message
	 */
	public DaoException(String message) {
		super(message);
	}

	/**
	 * Create a new DaoException.
	 * 
	 * @param cause
	 */
	public DaoException(Throwable cause) {
		super(cause);
	}

	/**
	 * Create a new DaoException.
	 * 
	 * @param message
	 * @param cause
	 */
	public DaoException(String message, Throwable cause) {
		super(message, cause);
	}

}