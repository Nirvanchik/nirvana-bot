package org.wikipedia.nirvana.parser.format;

/**
 * @author kmorozov
 */

public interface Format {

    enum FormatType {
        CSV("csv"),
        JSON("json"),
        HTML("json"),
        PAGE_PILE("pp"),
        TSV("tsv"),
        WIKI("wiki");

        private final String formatType;

        FormatType(String formatType) {
            this.formatType = formatType;
        }
    }

    FormatDescriptor getFormatDescriptor();

    FormatType getFormatType();
}
