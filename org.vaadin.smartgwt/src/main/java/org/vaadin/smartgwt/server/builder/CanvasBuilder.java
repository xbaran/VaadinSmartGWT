package org.vaadin.smartgwt.server.builder;

import org.vaadin.smartgwt.server.Canvas;
import org.vaadin.smartgwt.server.types.Alignment;

/**
 * Abstract builder that allows creation of Canvas instances by providing a fluent interface.  Regroups common properties for the Canvas class.
 * 
 * @param <T> the Canvas derivated class that this builder creates.
 * @param <B> the CanvasBuilder derivated class of this builder.
 */
abstract class CanvasBuilder<T extends Canvas, B extends CanvasBuilder<T, B>> extends BaseWidgetBuilder<T, B> {
	protected CanvasBuilder(T instance) {
		super(instance);
	}

	/**
	 * see {@link Canvas#setWidth(int)} 
	 */
	public B setWidth(int width) {
		instance().setWidth(width);
		return me();
	}

	/**
	 * see {@link Canvas#setWidth(String)}
	 */
	public B setWidth(String width) {
		instance().setWidth(width);
		return me();
	}

	/**
	 * see {@link Canvas#setHeight(int)} 
	 */
	public B setHeight(int height) {
		instance().setHeight(height);
		return me();
	}

	/**
	 * see {@link Canvas#setHeight(String)} 
	 */
	public B setHeight(String height) {
		instance().setHeight(height);
		return me();
	}

	/**
	 * see {@link Canvas#setBackgroundColor(String)} 
	 */
	public B setBackgroundColor(String backgroundColor) {
		instance().setBackgroundColor(backgroundColor);
		return me();
	}

	/**
	 * see {@link Canvas#setAlign(Alignment)}
	 */
	public B setAlign(Alignment align) {
		instance().setAlign(align);
		return me();
	}

	/**
	 * see {@link Canvas#setLayoutAlign(Alignment)}
	 */
	public B setLayoutAlign(Alignment layoutAlign) {
		instance().setLayoutAlign(layoutAlign);
		return me();
	}
}