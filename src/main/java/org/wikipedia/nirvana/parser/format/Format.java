package org.wikipedia.nirvana.parser.format;

/**
 * @author kmorozov
 */
public interface Format {

    enum FormatType {
        CSV("csv"),
        TSV("tsv");

        private final String formatType;

        FormatType(String formatType) {
            this.formatType = formatType;
        }
    }

    FormatDescriptor getFormatDescriptor();

    FormatType getFormatType();
}
