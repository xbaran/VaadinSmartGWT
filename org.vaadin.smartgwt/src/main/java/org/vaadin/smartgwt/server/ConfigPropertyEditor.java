package org.vaadin.smartgwt.server;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.vaadin.smartgwt.server.grid.events.SelectionUpdatedEvent;
import org.vaadin.smartgwt.server.grid.events.SelectionUpdatedHandler;
import org.vaadin.smartgwt.server.tree.PropertyGrid;
import org.vaadin.smartgwt.server.tree.Tree;
import org.vaadin.smartgwt.server.tree.TreeNode;
import org.vaadin.smartgwt.server.types.TreeModelType;

import bsh.EvalError;
import bsh.Interpreter;

import com.google.common.base.Throwables;
import com.netappsid.utils.NAIDClassLoader;
import com.vaadin.terminal.gwt.server.WebApplicationContext;

public class ConfigPropertyEditor extends PropertyGrid
{
	private static NAIDClassLoader classLoader;

	private static NAIDClassLoader getClassLoader()
	{
		if (classLoader == null)
		{
			try
			{
				final URL configuratorURL = new File("/home/ebelanger/Desktop/webapp/WEB-INF/lib/com.netappsid.erp.configurator_3.2.4.jar").toURI().toURL();
				final URL configurationURL = new File("/home/ebelanger/Desktop/webapp/WEB-INF/lib/FranciaflexConfigurator_1.0.2.jar").toURI().toURL();
				return classLoader = new NAIDClassLoader(new URL[] { configuratorURL, configurationURL }, ConfigPropertyEditor.class.getClassLoader());
			}
			catch (Exception e)
			{
				throw Throwables.propagate(e);
			}
		}

		return classLoader;
	}

	private final Interpreter interpreter = new Interpreter();

	public ConfigPropertyEditor()
	{
		try
		{
			interpreter.setClassLoader(getClassLoader());
			interpreter.source("/home/ebelanger/Desktop/webapp/WEB-INF/lib/ConfigPropertyEditor.bsh");
			interpreter.set("configPropertyEditor", this);
		}
		catch (Exception e)
		{
			Throwables.propagate(e);
		}

		addSelectionUpdatedHandler(new SelectionUpdatedHandler()
			{
				@Override
				public void onSelectionUpdated(SelectionUpdatedEvent event)
				{
					System.out.println("la selection a change sur le client: " + getSelectedRecords()[0].getAttribute("binding"));
				}
			});
	}

	public void init(String prd)
	{ // , String variant, String locale) {
		if (prd == null)
		{
			throw new IllegalArgumentException("Product cannot be null");
		}

		try
		{
			WebApplicationContext context = (WebApplicationContext) getApplication().getContext();
			HttpSession session = context.getHttpSession();
			session.setAttribute("configurator", getCi());
			session.setAttribute("initialized", true);
			initConfigurator(prd);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		fetch();
	}

	public void fetch()
	{
		try
		{
			List<TreeNode> properties = fetchTreeNodes();

			setData(makeTree(properties.toArray(new TreeNode[0])));

		}
		catch (Exception e)
		{
			Throwables.propagate(e);
		}
	}

	@Override
	public void changeVariables(Object source, Map<String, Object> variables)
	{
		super.changeVariables(source, variables);

		if (variables.containsKey("id"))
		{
			String id = variables.get("id").toString();
			Object value = variables.get("value");

			if (id != null && value != null)
			{
				updateID(id, value);
			}
		}
	}

	private Tree makeTree(TreeNode[] list)
	{
		Tree tree = new Tree();
		tree.setModelType(TreeModelType.PARENT);
		tree.setNameProperty("name");
		tree.setIdField("id");
		tree.setParentIdField("parent");
		tree.setShowRoot(false);
		tree.setData(list);
		return tree;
	}

	private Object getCi() throws EvalError
	{
		return interpreter.get("configurator");
	}

	private List<TreeNode> fetchTreeNodes() throws EvalError
	{
		return (List<TreeNode>) interpreter.eval("fetchTreeNodes()");
	}

	private void initConfigurator(String prd) throws EvalError
	{
		interpreter.getNameSpace().invokeMethod("initConfigurator", new Object[] { prd }, interpreter);
	}

	private void updateID(String id, Object value)
	{
		try
		{
			interpreter.getNameSpace().invokeMethod("updateID", new Object[] { id, value }, interpreter);
		}
		catch (Exception e)
		{
			throw Throwables.propagate(e);
		}
	}
}
