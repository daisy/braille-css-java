package org.daisy.braille.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cz.vutbr.web.css.CSSProperty;
import cz.vutbr.web.css.Declaration;
import cz.vutbr.web.css.NodeData;
import cz.vutbr.web.css.SupportedCSS;
import cz.vutbr.web.css.Term;
import cz.vutbr.web.domassign.DeclarationTransformer;
import cz.vutbr.web.domassign.SingleMapNodeData;

public class SimpleInlineStyle extends SingleMapNodeData implements NodeData, Cloneable, Iterable<PropertyValue> {
	
	private final static SupportedCSS cssInstance = new SupportedBrailleCSS(true, false);
	private final static DeclarationTransformer transformerInstance = new BrailleCSSDeclarationTransformer(cssInstance);
	private final static BrailleCSSParserFactory parserFactory = new BrailleCSSParserFactory();
	
	public static final SimpleInlineStyle EMPTY = new SimpleInlineStyle((List<Declaration>)null);

	public SimpleInlineStyle(String style) {
		this(style, null);
	}
	
	public SimpleInlineStyle(String style, SimpleInlineStyle parentStyle) {
		this(
			(style != null && !"".equals(style)) ? parserFactory.parseSimpleInlineStyle(style) : (List<Declaration>)null,
			parentStyle);
	}
	
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations) {
		this(declarations, null);
	}
	
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations, SimpleInlineStyle parentStyle) {
		this(declarations, parentStyle, transformerInstance, cssInstance);
	}
	
	public SimpleInlineStyle(Iterable<? extends Declaration> declarations, SimpleInlineStyle parentStyle,
	                         DeclarationTransformer transformer, SupportedCSS css) {
		super(transformer, css);
		if (declarations != null)
			for (Declaration d : declarations)
				super.push(d);
		if (parentStyle != null) {
			super.inheritFrom(parentStyle);
			inherited = true;
		}
	}
	
	public Term<?> getValue(String name) {
		return getValue(name, true);
	}
	
	public void removeProperty(String name) {
		map.remove(name);
	}
	
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	public int size() {
		return map.size();
	}
	
	public Iterator<PropertyValue> iterator() {
		return new Iterator<PropertyValue>() {
			Iterator<String> props = SimpleInlineStyle.this.map.keySet().iterator();
			public boolean hasNext() {
				return props.hasNext();
			}
			public PropertyValue next() {
				return SimpleInlineStyle.this.get(props.next());
			}
			public void remove() {
				props.remove();
			}
		};
	}
	
	public PropertyValue get(String property) {
		CSSProperty p = getProperty(property);
		if (p == null)
			return null;
		Declaration d = getSourceDeclaration(property);
		if (d == null) throw new IllegalStateException(); // can not happen
		return new PropertyValue(property, p, getValue(property), d);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		List<String> keys = new ArrayList<String>(map.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			if (sb.length() > 0)
				sb.append("; ");
			sb.append(key).append(": ");
			Term<?> value = getValue(key);
			if (value != null)
				sb.append(value);
			else
				sb.append(getProperty(key)); }
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof SimpleInlineStyle) {
			SimpleInlineStyle that = (SimpleInlineStyle)other;
			Set<String> keys = map.keySet();
			if (!keys.equals(that.map.keySet()))
				return false;
			for (String key : keys) {
				Term<?> value = getValue(key);
				if ((value == null && that.getValue(key) != null)
				    || (value != null && !value.equals(that.getValue(key))))
					return false;
				CSSProperty prop = getProperty(key);
				if ((prop == null && that.getProperty(key) != null)
				    || (prop != null && !prop.equals(that.getProperty(key))))
					return false;
			}
			return true;
		}
		return false;
	}
	
	private boolean concretized = false;

	@Override
	public SimpleInlineStyle concretize() {
		if (concretized)
			return this;
		else {
			SimpleInlineStyle copy = (SimpleInlineStyle)clone();
			copy.noCopyConcretize();
			return copy;
		}
	}
	
	private void noCopyConcretize() {
		super.concretize();
		concretized = true;
	}

	private boolean inherited = false;
	
	@Override
	public SimpleInlineStyle inheritFrom(NodeData parent) throws ClassCastException {
		if (inherited)
			throw new UnsupportedOperationException("Can not inherit from more than one parent style");
		else if (concretized)
			throw new UnsupportedOperationException("Can not inherit from a parent style: 'inherit' values were already concretized.");
		else if (!(parent instanceof SimpleInlineStyle))
			throw new UnsupportedOperationException("Can only inherit from another SimpleInlineStyle");
		else {
			SimpleInlineStyle copy = (SimpleInlineStyle)clone();
			copy.noCopyInheritFrom(((SimpleInlineStyle)parent));
			return copy;
		}
	}

	private void noCopyInheritFrom(NodeData parent) {
		super.inheritFrom(parent);
		inherited = true;
	}

	@Override
	public NodeData push(Declaration d) {
		throw new UnsupportedOperationException();
	}
}
