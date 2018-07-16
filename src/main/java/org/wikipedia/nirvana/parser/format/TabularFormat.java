package org.wikipedia.nirvana.parser.format;

import static org.wikipedia.nirvana.parser.format.Format.FormatType.CSV;
import static org.wikipedia.nirvana.parser.format.Format.FormatType.TSV;
import static org.wikipedia.nirvana.parser.format.TabFormatDescriptor.*;

/**
 * @author kmorozov
 */
public class TabularFormat implements Format {

    public static final TabularFormat CSV_CATSCAN_FORMAT = new TabularFormat(TSV_CATSCAN, CSV);
    public static final TabularFormat TSV_CATSCAN2_FORMAT = new TabularFormat(TSV_CATSCAN2, TSV);
    public static final TabularFormat TSV_CATSCAN3_FORMAT = new TabularFormat(TSV_CATSCAN3, TSV);
    public static final TabularFormat TSV_PETSCAN_FORMAT = new TabularFormat(TSV_PETSCAN, TSV);

    private final TabFormatDescriptor descriptor;
    private final FormatType formatType;

    private TabularFormat(TabFormatDescriptor descriptor, FormatType formatType) {
        this.descriptor = descriptor;
        this.formatType = formatType;
    }

    @Override
    public TabFormatDescriptor getFormatDescriptor() {
        return descriptor;
    }

    @Override
    public FormatType getFormatType() {
        return formatType;
    }
}