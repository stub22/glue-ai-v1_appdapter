package ext.bundle.openconverters.osgi;

public class Activator extends ext.osgi.common.ExtBundleActivatorBase {

	@Override public void ensureExtClassesAreFindable() {
		if (isOSGIProperty("osgi-tests", true)) {
			debugLoaders(org.apache.poi.common.usermodel.Hyperlink.class);
			debugLoaders(org.openxmlformats.schemas.drawingml.x2006.chart.impl.CTArea3DChartImpl.class);
			debugLoaders(schemasMicrosoftComOfficeExcel.impl.ClientDataDocumentImpl.class);
			debugLoaders(au.com.bytecode.opencsv.bean.CsvToBean.class);
		}
	}

}
