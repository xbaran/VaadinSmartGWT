package org.vaadin.smartgwt.server;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.vaadin.smartgwt.server.grid.events.SelectionUpdatedEvent;
import org.vaadin.smartgwt.server.grid.events.SelectionUpdatedHandler;
import org.vaadin.smartgwt.server.tree.PropertyGrid;
import org.vaadin.smartgwt.server.tree.Tree;
import org.vaadin.smartgwt.server.tree.TreeNode;
import org.vaadin.smartgwt.server.types.TreeModelType;

import bsh.EvalError;
import bsh.Interpreter;

import com.google.common.base.Throwables;
import com.netappsid.configurator.IConfigurator;
import com.netappsid.utils.NAIDClassLoader;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

public class ConfigPropertyEditor extends PropertyGrid {
	private static final Logger LOGGER = Logger.getLogger(ConfigPropertyEditor.class);
	private static NAIDClassLoader classLoader;
	private RendererPanel renderer;

	public ConfigPropertyEditor(RendererPanel renderer) {
		this();
		this.renderer = renderer;
	}

	public static NAIDClassLoader getConfiguratorClassLoader() {
		if (classLoader == null) {
			try {
				final URL configuratorURL = new File(System.getProperty("configuratorPath")).toURI().toURL();
				final URL configurationURL = new File(System.getProperty("configurationPath")).toURI().toURL();
				return classLoader = new NAIDClassLoader(new URL[] { configuratorURL, configurationURL }, ConfigPropertyEditor.class.getClassLoader());
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}

		return classLoader;
	}

	private final Interpreter interpreter = new Interpreter();

	public ConfigPropertyEditor() {
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		try {

			final InputStream scriptStream = getClass().getClassLoader().getResourceAsStream("/org/vaadin/smartgwt/ConfigPropertyEditor.bsh");
			NAIDClassLoader configuratorClassLoader = getConfiguratorClassLoader();

			configuratorClassLoader.loadClass("org.apache.derby.jdbc.AutoloadedDriver").getClassLoader();
			getClass().getClassLoader().loadClass("org.apache.derby.jdbc.AutoloadedDriver").getClassLoader();
			interpreter.setClassLoader(configuratorClassLoader);
			interpreter.eval(new InputStreamReader(scriptStream));
			interpreter.set("configPropertyEditor", this);
		} catch (Exception e) {
			Throwables.propagate(e);
		} finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
		addSelectionUpdatedHandler(new SelectionUpdatedHandler() {
			@Override
			public void onSelectionUpdated(SelectionUpdatedEvent event) {
				if (LOGGER.isDebugEnabled()) {
					final String sessionID = ((WebApplicationContext) getApplication().getContext()).getHttpSession().getId();
					LOGGER.debug(sessionID + " | " + getSelectedRecords()[0].getAttribute("binding"));
				}
			}
		});
	}

	public void init(String prd) { // , String variant, String locale) {
		if (prd == null) {
			throw new IllegalArgumentException("Product cannot be null");
		}

		try {
			WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
			HttpSession session = context.getHttpSession();
			session.setAttribute("configurator", getBSHConfigurator());
			session.setAttribute("initialized", false);
			initConfigurator(prd);
			fetch();
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	public void init(byte[] configurationBytes) {
		try {
			final IConfigurator configurator = getBSHConfigurator();
			final WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
			final HttpSession session = context.getHttpSession();

			configurator.setClassLoader(getConfiguratorClassLoader());
			configurator.deserialize(configurationBytes);
			session.setAttribute("configurator", configurator);
			session.setAttribute("initialized", false);
			initConfigurator();
			fetch();
		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	public IConfigurator getConfigurator() {
		try {
			final IConfigurator configurator = getBSHConfigurator();
			configurator.setClassLoader(getConfiguratorClassLoader());
			return configurator;
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
	}

	public void fetch() {
		try {
			final WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
			final HttpSession session = context.getHttpSession();

			List<TreeNode> properties = fetchTreeNodes();

			setData(makeTree(properties.toArray(new TreeNode[0])));
			session.setAttribute("initialized", true);
			renderer.refresh();

		} catch (Exception e) {
			Throwables.propagate(e);
		}
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables) {
		super.changeVariables(source, variables);

		if (variables.containsKey("id")) {
			String id = variables.get("id").toString();
			Object value = variables.get("value");

			if (id != null && value != null) {
				updateID(id, value);
			}
		}
	}

	private Tree makeTree(TreeNode[] list) {
		Tree tree = new Tree();
		tree.setModelType(TreeModelType.PARENT);
		tree.setNameProperty("name");
		tree.setIdField("id");
		tree.setParentIdField("parent");
		tree.setShowRoot(false);
		tree.setData(list);
		return tree;
	}

	private IConfigurator getBSHConfigurator() throws EvalError {
		return (IConfigurator) interpreter.get("configurator");
	}

	private List<TreeNode> fetchTreeNodes() throws EvalError {
		return invokeMethod("fetchTreeNodes");
	}

	private void initConfigurator(String prd) throws EvalError {
		invokeMethod("initConfigurator", prd);
	}

	private void initConfigurator() throws EvalError {
		invokeMethod("initConfigurator");
	}

	private void updateID(String id, Object value) {
		if (LOGGER.isDebugEnabled()) {
			final String sessionID = ((WebApplicationContext) getApplication().getContext()).getHttpSession().getId();
			LOGGER.debug(sessionID + " | " + "updateID(id='" + id + "', value='" + value + "');");
		}

		invokeMethod("updateID", id, value);
	}

	public void resetOverride(String id) {
		invokeMethod("resetOverride", id);
	}

	public Boolean isOverriden(String id) {
		return invokeMethod("isOverriden", id);
	}

	private <T> T invokeMethod(String name, Object... params) {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

		try {
			NAIDClassLoader configuratorClassLoader = getConfiguratorClassLoader();
			Thread.currentThread().setContextClassLoader(configuratorClassLoader);
			return (T) interpreter.getNameSpace().invokeMethod(name, params, interpreter);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		} finally {
			Thread.currentThread().setContextClassLoader(classLoader);
		}
	}
}
