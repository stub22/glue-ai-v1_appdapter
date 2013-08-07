package org.appdapter.bind.rdf.jena.assembly;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.appdapter.api.trigger.AnyOper.HasIdent;
import org.appdapter.bind.rdf.jena.model.JenaLiteralUtils;
import org.appdapter.core.component.KnownComponent;
import org.appdapter.core.convert.Converter;
import org.appdapter.core.convert.NoSuchConversionException;
import org.appdapter.core.convert.ReflectUtils;
import org.appdapter.core.item.Item;
import org.appdapter.core.item.JenaResourceItem;
import org.appdapter.core.log.Debuggable;
import org.appdapter.core.name.FreeIdent;
import org.appdapter.core.name.Ident;
import org.appdapter.core.name.ModelIdent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.assembler.Assembler;
import com.hp.hpl.jena.assembler.Mode;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class AssemblerConverter implements Converter {
	static Logger theLogger = LoggerFactory.getLogger(AssemblerConverter.class);

	public AssemblerConverter(Assembler asmblr, Mode mode) {
		// TODO Auto-generated constructor stub
	}

	@Override public <T> T convert(Object obj, Class<T> objNeedsToBe, int maxCvt) throws NoSuchConversionException {
		try {
			Object eval = JenaLiteralUtils.convertOrNull(obj, objNeedsToBe, maxCvt);
			if (objNeedsToBe.isInstance(eval))
				return (T) eval;
			throw new NoSuchConversionException(obj, objNeedsToBe, null);
		} catch (Throwable e) {
			throw new NoSuchConversionException(obj, objNeedsToBe, e);
		}
	}

	public static AssemblerConverter makeConverter(Assembler asmblr, Mode mode) {
		return new AssemblerConverter(asmblr, mode);
	}

	public static void initObjectProperties(Object target, Item item, Assembler asmblr, Mode mode, ItemAssemblyReader reader, Class tafc) {
		JenaResourceItem resourceItem = null;
		ArrayList<Throwable> oops = null;
		int missedCount = 0;
		AssemblerConverter converter = makeConverter(asmblr, mode);
		if (item instanceof JenaResourceItem) {
			resourceItem = (JenaResourceItem) item;
			Map<Property, List<RDFNode>> properties = resourceItem.getPropertyMap();
			for (Map.Entry<Property, List<RDFNode>> e : properties.entrySet()) {
				//rdf:type is used by the jena assembler and we should ignore it                
				if ("type".equals(e.getKey().getLocalName())) {
					continue;
				}
				try {
					if (!ReflectUtils.setObjectPropertyValue(target, tafc, e.getKey().getLocalName(), converter, e.getValue(), true, true))
						missedCount++;
				} catch (Throwable t) {
					t.printStackTrace();
					if (oops == null)
						oops = new ArrayList<Throwable>();
					oops.add(t);
				}
			}
			if (oops == null && missedCount == 0)
				return;
		}
		BeanInfo info;
		try {
			info = Introspector.getBeanInfo(tafc);
			Ident mainIdent = item.getIdent();

			for (PropertyDescriptor pd : info.getPropertyDescriptors()) {
				String pdn = pd.getName();
				pdn = pdn.substring(0, 1).toLowerCase() + pdn.substring(1);
				Class pdt = pd.getPropertyType();
				setObjectField(target, item, reader, pd.getWriteMethod(), pdn, pdt, asmblr, mode);
			}
		} catch (Throwable e) {
			throw Debuggable.reThrowable(e);
		}
	}

	public static void setObjectField(Object thingActionFilterImpl, Item item, ItemAssemblyReader reader, Method writeMethod, String pdn, Class pdt, Assembler asmblr, Mode mode)
			throws IllegalAccessException, InvocationTargetException {
		if (pdt == null) {
			if (writeMethod != null) {
				pdt = writeMethod.getParameterTypes()[0];
			} else {
				pdt = Object.class;
			}
		}
		pdt = ReflectUtils.nonPrimitiveTypeFor(pdt);
		String sv = reader.readConfigValString(item.getIdent(), pdn, item, null);
		if (sv == null) {
			List<Object> res = reader.findOrMakeLinkedObjects(item, pdn, asmblr, mode, null);
			return;
		}
		if (writeMethod == null) {
			theLogger.warn("Missing write method on field: " + pdn + " type " + pdt.getSimpleName() + " = " + sv);
			return;
		}
		theLogger.warn("Setting field: " + pdn + " type " + pdt.getSimpleName() + " = " + sv);
		if (pdt == String.class) {
			writeMethod.invoke(thingActionFilterImpl, sv);
		} else if (pdt == Ident.class) {
			writeMethod.invoke(thingActionFilterImpl, new FreeIdent(sv));
		} else {

		}
	}

	@Override public Integer declaresConverts(Object obj, Class objClass, Class objNeedsToBe, int maxCvt) {
		if (obj instanceof RDFNode || obj instanceof HasIdent)
			return MIGHT;
		return WONT;
	}
}
