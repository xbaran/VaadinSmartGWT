/*
 * SmartGWT (GWT for SmartClient)
 * Copyright 2008 and beyond, Isomorphic Software, Inc.
 *
 * SmartGWT is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.  SmartGWT is also
 * available under typical commercial license terms - see
 * http://smartclient.com/license
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 */
package org.vaadin.smartgwt.server.data;

import java.util.Map;

import org.vaadin.smartgwt.server.core.RefDataClass;

import com.google.common.collect.Maps;

/**
 * A Record contains attributes that are displayed and edited by a {@link com.smartgwt.client.widgets.DataBoundComponent}.
 * <p/>
 * DataBoundComponents have a concept of named fields, where values for each field are found
 * under the same-named attribute in a Record.
 * <p/>
 * The concept of working with Records is common to all DataBoundComponents, although individual
 * DataBoundComponents may work with singular records ({@link com.smartgwt.client.widgets.form.DynamicForm}) or may work with lists
 * ({@link com.smartgwt.client.widgets.grid.ListGrid}), trees ({@link com.smartgwt.client.widgets.tree.TreeGrid}), or cubes
 * (CubeGrid) of records.
 * <p/>
 * A Record is always the same type of Java object regardless of how the record is loaded (static
 * data, java server, XML web service, etc).  However, individual DataBoundComponents may also
 * look for special attributes on Records which control styling or behavior.  For convenience,
 * there are subclasses of Record with type-safe setters for such attributes (such as {@link
 * com.smartgwt.client.widgets.grid.ListGrid#setRecordEditProperty(String)}).  In reality, all
 * such subclasses are wrappers over the same underlying data object, and you can convert to
 * whichever wrapper is most convenient via:
 * <pre>
 *    new ListGridRecord(recordInstance.getJsObj());
 * </pre>
 * You can also create your own subclass of Record with type-specific getters and setters,
 * however, if you do so, you should store values via setAttribute() and retrieve them via
 * getAttribute() rather than keeping values as normal Java properties.  Only attributes will
 * be visible to DataBoundComponents, ordinary Java properties will not. 
 * <p/>
 * Note that directly changing an attribute of a Record via setAttribute() will not notify any
 * DataBoundComponents that the Record has changed or cause any kind of persistence operation
 * to occur.  Instead, use component-specific methods such as DynamicForm.setValue() or
 * ListGrid.setEditValue() to explicitly tell the components about a change that should be
 * saved.
 */
public class Record extends RefDataClass {
	/**
	 * Convert this record to a Map. This is a recursive conversion so if an attribute on this record is set to
	 * another Record instance it will also be converted to a Map.
	 * @return
	 */
	public Map toMap() {
		final Map<Object, Object> mapped = Maps.newHashMap();
		for (String name : getAttributes()) {
			mapped.put(name, getAttributeAsObject(name));
		}
		return mapped;
	}
}
