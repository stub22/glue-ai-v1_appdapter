package ext.bundle.openconverters.osgi;

import org.osgi.framework.BundleContext;

public class Activator extends ext.osgi.common.ExtBundleActivatorBase {

	public static void ensureConvertersClassesAreFindable() {
		debugLoaders(org.apache.poi.common.usermodel.Hyperlink.class);
		debugLoaders(org.openxmlformats.schemas.drawingml.x2006.chart.impl.CTArea3DChartImpl.class);
		debugLoaders(schemasMicrosoftComOfficeExcel.impl.ClientDataDocumentImpl.class);
		debugLoaders(au.com.bytecode.opencsv.bean.CsvToBean.class);
	}

	@Override protected void handleFrameworkStartedEvent(BundleContext bundleCtx) throws Exception {
		debugLoaders(Activator.class);
		super.handleFrameworkStartedEvent(bundleCtx);
	}

}
